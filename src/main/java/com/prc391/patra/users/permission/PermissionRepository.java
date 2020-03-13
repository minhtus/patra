package com.prc391.patra.users.permission;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PermissionRepository extends MongoRepository<Permission, Long> {

}
