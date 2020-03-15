package com.prc391.patra.lists;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class List {
    @Id
    @JsonIgnore
    private String listId;
    private String listName;
    private String listDescription;
    private String creatorUsername;
}
