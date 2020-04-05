package com.prc391.patra.tasks;

import java.util.List;

public interface TaskRepositoryCustom {
    boolean updateAssignee(String taskId, List<String> memberIds);

    boolean removeAssigneeInMultipleTask(List<String> taskIds, List<String> memberIds);

    boolean removeAssignee(String taskId, List<String> memberIds);
    boolean updateAttachImage(String taskId, String imagePath);
}
