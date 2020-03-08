package com.prc391.patra.users;

import org.springframework.data.mongodb.repository.MongoRepository;

interface UserRepository extends MongoRepository<User, String> {
    User getUserByUsername(String username);
    User save(User user);
}
