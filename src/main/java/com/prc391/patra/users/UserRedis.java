package com.prc391.patra.users;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

//redis commands:
//SMEMBERS User -> return list of usernames
//HGETALL User:username -> return the map which contains the user details (fields below)
//HGET User:username fieldname -> return the field value
//H stands for Hash
@RedisHash(value = "User")
//@RedisHash(value = "User", timeToLive = 500)
@Data
public class UserRedis implements Serializable {
    @Id
    private String username;

//    public UserRedis() {
//    }
//
//    @PersistenceConstructor
//    public UserRedis(String username, String email, String name, String enabled, String currMemberId) {
//        this.username = username;
//        this.email = email;
//        this.name = name;
//        this.enabled = enabled;
//        this.currMemberId = currMemberId;
//    }

    //@Indexed helps findByEmail in repository
    @Indexed
    private String email;
    private String name;
    private String enabled;
    private String currMemberId;
}
