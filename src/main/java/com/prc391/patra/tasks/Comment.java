package com.prc391.patra.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class Comment {
    @JsonIgnore
    private String commentId;
    private String username;
    private String comment;
    @JsonIgnore
    private long commentTimestamp;
    @JsonIgnore
    private String[] attachmentsPath;
}
