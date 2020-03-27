package com.prc391.patra.sheets;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SheetRepository extends MongoRepository<Sheet, String> {
    List<Sheet> getAllByOrgIdIn(String orgID);
}
