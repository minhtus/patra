package com.prc391.patra.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    @Id
    private String taskId;
    private String listId;
    private String creatorUsername;
    private String[] assigneeUsername;
    private String taskName;
    private String taskDescription;
    private String taskDetails;
    private int statusId;
    private long dueDate;
    private Comment[] comments;
}
