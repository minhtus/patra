package com.prc391.patra.users;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//redis commands:
//SMEMBERS User -> return list of usernames
//HGETALL User:username -> return the map which contains the user details (fields below)
//HGET User:username fieldname -> return the field value
//H stands for Hash
@RedisHash(value = "User", timeToLive = 604800)
@Data
public class UserRedis implements Serializable {
    @Id
    private String username;
    @Indexed
    private String email;
    private String name;
    private String enabled;
    private Map<String, String> orgPermissions;
}
