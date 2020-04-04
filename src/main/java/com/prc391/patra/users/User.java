package com.prc391.patra.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class User {
    @Id
    private String username;
    @JsonIgnore
    private String passHash;
    @Indexed(unique = true, name = "user_email_index")
    private String email;
    private String name;

//    @JsonIgnore
//    @Indexed(unique = true, name = "google_user_id_index")
//    private String googleUserId;

    @JsonIgnore
    private boolean enabled;

    private String currMemberId;

    private String imageUrl;
}
