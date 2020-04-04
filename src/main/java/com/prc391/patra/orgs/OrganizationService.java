package com.prc391.patra.orgs;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.ControllerSupportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationUtils authorizationUtils;
    private final UserRedisRepository userRedisRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, MemberRepository memberRepository, AuthorizationUtils authorizationUtils, UserRedisRepository userRedisRepository) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.authorizationUtils = authorizationUtils;
        this.userRedisRepository = userRedisRepository;
    }

    public List<Organization> getAllOrg() {
        return organizationRepository.findAll();
    }

    public Organization getOrganization(String id) throws EntityNotFoundException {
        Optional<Organization> optionalOrg = organizationRepository.findById(id);
        if (!optionalOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        return optionalOrg.get();
    }

    public List<Member> getAllMemberFromOrgId(String id) throws EntityNotFoundException {
        Optional<Organization> optionalOrg = organizationRepository.findById(id);
        if (!optionalOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        List<Member> memberList = memberRepository.getAllByOrgId(id);
        if (CollectionUtils.isEmpty(memberList)) {
            throw new EntityNotFoundException("There are no Member in this organization.");
        }
        return memberList;
    }

    public Organization insertOrganization(Organization newOrg) throws UnauthorizedException {
        Organization organization = organizationRepository.save(newOrg);
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member member = new Member();
        member.setUsername(principal.getUsername());
        member.setOrgId(organization.getOrgId());
        member.setPermission(SecurityConstants.ADMIN_ACCESS);
        memberRepository.save(member);
        updateUserInRedis(principal.getUsername());
        return organization;
    }

    public Organization updateOrganization(String id, Organization updateOrg) throws EntityNotFoundException, UnauthorizedException {
        Optional<Organization> optionalCurrOrg = organizationRepository.findById(id);
        if (!optionalCurrOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        if (!authorizationUtils.authorizeAccess(id, SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        Organization currOrg = optionalCurrOrg.get();
        currOrg.mergeForUpdate(updateOrg);
        return organizationRepository.save(currOrg);
    }

    public void deleteOrganization(String id) throws EntityNotFoundException, UnauthorizedException {
        Optional<Organization> optionalCurrOrg = organizationRepository.findById(id);
        if (!optionalCurrOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        if (!authorizationUtils.authorizeAccess(id, SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member oldMember = memberRepository.getByUsernameAndOrgId(principal.getUsername(), id);
        if (!ObjectUtils.isEmpty(oldMember)) {
            memberRepository.deleteById(oldMember.getMemberId());
        }
        updateUserInRedis(principal.getUsername());
        organizationRepository.deleteById(id);
    }

    private void updateUserInRedis(String username) {
        UserRedis userInRedis = userRedisRepository.findById(username).get();
        userRedisRepository.deleteById(username);
        Map<String, String> orgPermissions = memberRepository.getAllByUsername(username).stream()
                .collect(Collectors.toMap(Member::getOrgId, Member::getPermission));
        userInRedis.setOrgPermissions(orgPermissions);
        userRedisRepository.save(userInRedis);
    }
}
