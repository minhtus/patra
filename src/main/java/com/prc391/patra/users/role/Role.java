package com.prc391.patra.users.role;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class Role {
    @Id
    private long id;
    private String name;
    private List<Long> permissions;
}
