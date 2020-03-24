package com.prc391.patra.lists.requests;

import lombok.Data;

@Data
public class CreateListRequest {
    private String orgId;
    private String listName;
    private String listDescription;
    private String reporter;
    private boolean privateList;

}
