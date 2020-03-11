package com.prc391.patra.orgs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
public class Organization {
    @MongoId
    private String id;
    @Field("org_name")
    private String name;
}
