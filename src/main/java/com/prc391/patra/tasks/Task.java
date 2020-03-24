package com.prc391.patra.tasks;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Document
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {
    @Id
    private String taskId;
    private String listId;
    private String reporter;
//    private String[] assigneeMemberId;
    //Member, not User
    private List<String> assignee;
    private String taskName;
    private String taskDescription;
    private String taskDetails;
    private int statusId;
    private long dueDate;
    private Comment[] comments;

    void mergeForUpdate(Task other) {
        this.taskName = other.taskName != null ? other.taskName : this.taskName;
        this.taskDescription = other.taskDescription != null ? other.taskDescription : this.taskDescription;
        this.taskDetails = other.taskDetails != null ? other.taskDetails : this.taskDetails;
        this.dueDate = other.dueDate > 0 ? other.dueDate : this.dueDate;
        this.assignee = !CollectionUtils.isEmpty(other.assignee) ? other.assignee : this.assignee;
    }
}
