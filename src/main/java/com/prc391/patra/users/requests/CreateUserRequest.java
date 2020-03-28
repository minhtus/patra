package com.prc391.patra.users.requests;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String name;
    //newly created user won't have any member id
    //this field is useless
//    private String currMemberId;
}
