package com.prc391.patra.users.request;

import lombok.Data;

@Data
public class UserResponse {
    private String id;
    private String email;
    private String name;

    private boolean enabled;

    //TODO: redesign the Permission.
    private String currMemberId;
}
