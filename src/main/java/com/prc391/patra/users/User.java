package com.prc391.patra.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
public class User {
    @Id
    @JsonIgnore
    private String id;
    private String username;
    private String passHash;
    private String email;
    private String name;

    private boolean enabled;

    //TODO: redesign the Permission. This is temporary
    private String currMemberId;

//    private List<Long> roles;
}
