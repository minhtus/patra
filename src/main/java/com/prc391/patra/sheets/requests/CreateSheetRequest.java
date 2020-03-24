package com.prc391.patra.sheets.requests;

import lombok.Data;

@Data
public class CreateSheetRequest {
    private String orgId;
    private String listName;
    private String listDescription;
    private String reporter;
    private boolean privateList;

}
