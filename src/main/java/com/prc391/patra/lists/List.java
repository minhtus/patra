package com.prc391.patra.lists;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class List {
    @Id
    private String listId;
    private String orgId;
    private String listName;
    private String listDescription;
    private String reporterMemberId;
    private boolean privateList;
    private String[] assignedMemberIds;
}
