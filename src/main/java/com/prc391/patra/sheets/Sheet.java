package com.prc391.patra.sheets;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Sheet {
    @Id
    private String sheetId;
    private String orgId;
    private String sheetName;
    private String sheetDescription;
    private String reporter;
    private boolean privateSheet;
    private String[] assignee;
}
