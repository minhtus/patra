package com.prc391.patra.tasks;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public
class Comment {
    private String username;
    private String comment;
    private String[] attachmentsPath;
}
