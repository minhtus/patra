package com.prc391.patra.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JwtRedisService {

    private final JwtRedisRepository jwtRedisRepository;

    @Autowired
    public JwtRedisService(JwtRedisRepository jwtRedisRepository) {
        this.jwtRedisRepository = jwtRedisRepository;
    }

    public boolean saveToRedisBlacklist(String jwtString) {
        Optional<JwtRedis> optionalJwt = jwtRedisRepository.findById(jwtString);
        if (optionalJwt.isPresent()) {
            return false;
        }
        JwtRedis jwtRedis = new JwtRedis();
        jwtRedis.setJwtString(jwtString);
        jwtRedisRepository.save(jwtRedis);
        return true;
    }

    public boolean isExistInBlacklist(String jwtString) {
        Optional<JwtRedis> optionalJwt = jwtRedisRepository.findById(jwtString);
        if (optionalJwt.isPresent()) {
            return true;
        }
        return false;
    }
}
