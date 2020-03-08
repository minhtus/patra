package com.prc391.patra.orgs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Getter
@Setter
public class Organization {
    @MongoId
    @Field("org_id")
    private String id;
    @Field("org_name")
    private String name;
}
