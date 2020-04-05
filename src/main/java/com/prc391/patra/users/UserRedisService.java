package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRedisService {
    private final UserRedisRepository userRedisRepository;

    @Autowired
    public UserRedisService(UserRedisRepository userRedisRepository) {
        this.userRedisRepository = userRedisRepository;
    }

    public UserRedis getUserRedis(String username) throws EntityNotFoundException {
        return userRedisRepository.findById(username).orElseThrow(EntityNotFoundException::new);
    }

    public boolean deleteUserInRedis(String username) {
        Optional<UserRedis> optionalUserRedis = userRedisRepository.findById(username);
        if (!optionalUserRedis.isPresent()) {
            return false;
        }
        userRedisRepository.deleteById(username);
        return true;
    }
}
