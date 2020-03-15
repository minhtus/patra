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
    private String id;
    private String passHash;
    private String email;
    private String name;

    private boolean enabled;

    //TODO: redesign the Permission.
    private String currMemberId;

    //temporary remove Role functionality
//    private List<Long> roles;
}
