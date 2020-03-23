package com.prc391.patra.users.requests;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String name;
    private String currMemberId;
}
