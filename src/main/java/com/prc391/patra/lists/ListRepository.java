package com.prc391.patra.lists;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ListRepository extends MongoRepository<List, String> {
}
