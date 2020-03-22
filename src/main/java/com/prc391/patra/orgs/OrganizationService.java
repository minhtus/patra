package com.prc391.patra.orgs;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.orgs.requests.CreateOrganizationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Autowired
    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public Organization getOrganization(String id) throws EntityNotFoundException {
        Optional<Organization> optionalOrg = organizationRepository.findById(id);
        if (!optionalOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        return optionalOrg.get();
    }

    public Organization insertOrganization(Organization newOrg) {

        return organizationRepository.save(newOrg);
    }

    public Organization updateOrganization(String id, Organization updateOrg) throws EntityNotFoundException {
        Optional<Organization> optionalCurrOrg = organizationRepository.findById(id);
        if (!optionalCurrOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        Organization currOrg = optionalCurrOrg.get();
        currOrg.mergeForUpdate(updateOrg);
        return organizationRepository.save(currOrg);
    }

    public void deleteOrganization(String id) throws EntityNotFoundException {
        Optional<Organization> optionalCurrOrg = organizationRepository.findById(id);
        if (!optionalCurrOrg.isPresent()) {
            throw new EntityNotFoundException("Organization with id " + id + " not exist.");
        }
        organizationRepository.deleteById(id);
    }
}
