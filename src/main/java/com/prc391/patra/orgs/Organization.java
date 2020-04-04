package com.prc391.patra.orgs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Organization {
    @Id
    private String orgId;
    private String name;
    private String imageUrl;
    private String orgCreator;

    public void mergeForUpdate(Organization other) {
        this.orgId = other.orgId != null? other.orgId : this.orgId;
        this.name = other.name != null? other.name : this.name;
        this.imageUrl = other.imageUrl != null? other.imageUrl : this.imageUrl;
        this.orgCreator = other.orgCreator != null? other.orgCreator : this.orgCreator;
    }
}
