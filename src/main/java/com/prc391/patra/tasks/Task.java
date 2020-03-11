package com.prc391.patra.tasks;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
class Task {
    @MongoId
    private String taskId;
    private String listId;
    private String taskName;
    private String taskDescription;
    private String taskDetails;
    private int status;
    private long dueDate;
}
