package com.prc391.patra.tasks;

import java.util.List;

public interface TaskRepositoryCustom {
    boolean updateAssignee(String taskId, List<String> memberIds);
}
