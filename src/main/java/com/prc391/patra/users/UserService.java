package com.prc391.patra.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    User registerUser(User user) {
        return userRepository.save(user);
    }

    User getUser(String username) {
        return userRepository.getUserByUsername(username);
    }

}
