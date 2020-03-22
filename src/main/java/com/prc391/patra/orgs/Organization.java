package com.prc391.patra.orgs;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
public class Organization {
    @Id
    private String id;
    private String name;
    private String imageUrl;

    public void mergeForUpdate(Organization org) {
        this.id = org.id != null? org.id : this.id;
        this.name = org.name != null? org.name : this.name;
        this.imageUrl = org.imageUrl != null? org.imageUrl : this.imageUrl;
    }
}
