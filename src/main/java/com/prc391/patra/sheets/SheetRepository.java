package com.prc391.patra.sheets;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SheetRepository extends MongoRepository<Sheet, String> {
}
