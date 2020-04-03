package com.prc391.patra.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {
    private String commentId;
    private String username;
    private String memberId;
    private String comment;
    private String[] imagesPath;
}
