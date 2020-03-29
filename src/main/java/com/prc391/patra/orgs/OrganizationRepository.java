package com.prc391.patra.orgs;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
    List<Organization> getAllByOrgIdIn(List<String> orgIds);
}
