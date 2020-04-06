package com.prc391.patra.orgs;

import com.prc391.patra.members.responses.MemberResponse;
import com.prc391.patra.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.ControllerSupportUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationUtils authorizationUtils;
    private final UserRedisRepository userRedisRepository;
    private final ModelMapper mapper;
    private final UserRepository userRepository;

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

    public List<MemberResponse> getAllMemberFromOrgId(String id) throws EntityNotFoundException {
        Optional<Organization> optionalOrg = organizationRepository.findById(id);
        if (!optionalOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        List<Member> memberList = memberRepository.getAllByOrgId(id);
        if (CollectionUtils.isEmpty(memberList)) {
            throw new EntityNotFoundException("There are no Member in this organization.");
        }
        List<MemberResponse> memberResponseList = new ArrayList<>();
        for (Member member : memberList) {
            Optional<User> optionalUser = userRepository.findById(member.getUsername());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                MemberResponse memberResponse = mapper.map(member, MemberResponse.class);
                memberResponse.setFullName(user.getName());
                memberResponseList.add(memberResponse);
                memberResponse.setOrganization(optionalOrg.get());
            }
        }
        return memberResponseList;
    }

    public Organization insertOrganization(Organization newOrg) throws UnauthorizedException {
        Organization organization = organizationRepository.save(newOrg);
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member member = new Member();
        member.setUsername(principal.getUsername());
        member.setOrgId(organization.getOrgId());
        member.setPermission(SecurityConstants.ADMIN_ACCESS);
        memberRepository.save(member);
        organization.setOrgCreator(member.getMemberId());
        updateUserInRedis(principal.getUsername());
        return organizationRepository.save(organization);
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
        userRedisRepository.deleteById(username);
        UserRedis userInRedis = mapper.map(userRepository.findById(username).get(), UserRedis.class);
        Map<String, String> orgPermissions = memberRepository.getAllByUsername(username).stream()
                .collect(Collectors.toMap(Member::getOrgId, Member::getPermission));
        userInRedis.setOrgPermissions(orgPermissions);
        userRedisRepository.save(userInRedis);
    }
}
