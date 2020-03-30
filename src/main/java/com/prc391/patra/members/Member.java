package com.prc391.patra.members;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member {
    @Id
    private String memberId;
    private String orgId;
    private String username;
    private Long[] permissions;
    private List<String> assignedTaskId;

    public void mergeToUpdate(Member other) {
        this.memberId = other.memberId != null ? other.memberId :  this.memberId;
        this.orgId = other.orgId != null ? other.orgId : this.orgId;
        this.username = other.username != null ? other.username : this.username;
        this.permissions = other.permissions != null ? other.permissions : this.permissions;
    }
}
