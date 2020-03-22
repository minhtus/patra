package com.prc391.patra.users.permission;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PermissionRepository extends MongoRepository<Permission, Long> {
    Permission getByName(String name);

    List<Permission> getByIdIn(List<Long> ids);
}
