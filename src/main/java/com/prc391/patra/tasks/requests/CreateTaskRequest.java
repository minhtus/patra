package com.prc391.patra.tasks.requests;

import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {
    private String listId;
    private String reporterMemberId;
    private String taskName;
    private String taskDescription;
    private String taskDetails;
    private long dueDate;
    private List<String> assigneeMemberId;
}
