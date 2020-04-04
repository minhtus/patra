package com.prc391.patra.users;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class GoogleUser {
    @Id
    private String googleUserId;
//    @Indexed(unique = true)
    private String email;
    private String name;
    private  String imageUrl;
}
