package com.prc391.patra.jwt;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "JWT", timeToLive = 604800)
@Data
public class JwtRedis {
    @Id
    private String jwtString;
}
