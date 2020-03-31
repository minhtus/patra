package com.prc391.patra.users;

import org.springframework.data.repository.CrudRepository;

//@Repository
public interface UserRedisRepository extends CrudRepository<UserRedis, String> {
    UserRedis findByEmail(String email);
}
