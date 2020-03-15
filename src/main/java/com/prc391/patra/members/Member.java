package com.prc391.patra.members;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Member {
    @Id
    private String memberId;
    private String orgId;
    private String username;
    private Long[] permissions;
}
