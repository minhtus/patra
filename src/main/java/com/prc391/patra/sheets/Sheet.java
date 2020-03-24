package com.prc391.patra.sheets;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Sheet {
    @Id
    private String listId;
    private String orgId;
    private String listName;
    private String listDescription;
    private String reporter;
    private boolean privateList;
    private String[] assignee;
}
