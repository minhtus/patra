package com.prc391.patra.sheets.requests;

import lombok.Data;

@Data
public class CreateSheetRequest {
    private String orgId;
    private String sheetName;
    private String sheetDescription;
    private String reporter;
    private boolean privateSheet;

}
