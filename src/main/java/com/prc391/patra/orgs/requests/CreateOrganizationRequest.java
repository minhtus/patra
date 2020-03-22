package com.prc391.patra.orgs.requests;

import lombok.Data;

@Data
public class CreateOrganizationRequest {
    private String name;
    private String imageUrl;
}
