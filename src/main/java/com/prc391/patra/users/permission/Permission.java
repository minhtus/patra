package com.prc391.patra.users.permission;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Permission {
    @Id
    private long id;
    private String name;
}
