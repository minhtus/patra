package com.prc391.patra.users;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
class User {
    @Id
    private long id;
    private String username;
    private String passHash;
    private String name;
}
