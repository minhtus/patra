package com.prc391.patra.members.responses;

import com.prc391.patra.orgs.Organization;
import lombok.Data;

import java.util.List;

@Data
public class MemberResponse {
    private String memberId;
    private String orgId;
    private String username;
    private String permission;
    private List<String> assignedTaskId;
    private Organization organization;
    private String fullName;
}
