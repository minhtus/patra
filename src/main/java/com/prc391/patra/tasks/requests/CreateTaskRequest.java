package com.prc391.patra.tasks.requests;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private String listId;
    private String reporter;
    private String taskName;
    private String taskDescription;
    private String taskDetails;
    private long dueDate;
}
