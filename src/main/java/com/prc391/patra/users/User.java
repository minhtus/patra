package com.prc391.patra.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
class User {
    @MongoId
    private long id;
    private String username;
    private String passHash;
    private String name;
}
