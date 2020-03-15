package com.prc391.patra.users.role;

import com.prc391.patra.users.permission.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RoleRepository extends MongoRepository<Role, Long> {
    Role getRoleByName(String name);
}
