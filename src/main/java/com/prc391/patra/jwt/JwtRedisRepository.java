package com.prc391.patra.jwt;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JwtRedisRepository extends CrudRepository<JwtRedis, String> {
}
