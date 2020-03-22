package com.prc391.patra.orgs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization {
    @Id
    private String id;
    private String name;
    private String imageUrl;

    public void mergeForUpdate(Organization other) {
        this.id = other.id != null? other.id : this.id;
        this.name = other.name != null? other.name : this.name;
        this.imageUrl = other.imageUrl != null? other.imageUrl : this.imageUrl;
    }
}
