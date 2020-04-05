package com.prc391.patra.members;

import java.util.List;

public interface MemberRepositoryCustom {
    boolean updateAssignedTask(String memberId, List<String> taskIds);
    boolean updateAssignedTaskMultipleUser(List<String> memberIds, List<String> taskIds);
    boolean removeAssignedTask(List<String> memberIds, List<String> taskIds);
}
