package com.prc391.patra.users.requests;

import lombok.Data;

@Data
public class CreateGoogleUserRequest {
    private String googleIdToken;
}
