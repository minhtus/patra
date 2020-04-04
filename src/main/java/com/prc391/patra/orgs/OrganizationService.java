package com.prc391.patra.orgs;

import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final MemberRepository memberRepository;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository, MemberRepository memberRepository, AuthorizationUtils authorizationUtils) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.authorizationUtils = authorizationUtils;
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

    public Organization insertOrganization(Organization newOrg) {

        return organizationRepository.save(newOrg);
    }

    public Organization updateOrganization(String id, Organization updateOrg) throws EntityNotFoundException, UnauthorizedException {
        Optional<Organization> optionalCurrOrg = organizationRepository.findById(id);
        if (!optionalCurrOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        if (!authorizationUtils.authorizeAccess(id,SecurityConstants.ADMIN_ACCESS)) {
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
        if (!authorizationUtils.authorizeAccess(id,SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        organizationRepository.deleteById(id);
    }
}
