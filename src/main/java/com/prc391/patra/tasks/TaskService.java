package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    //    @PostAuthorize("#Collections.contains(#Arrays.asList(#returnObject.assigneeMemberId), authentication.principal.currMemberId)")
    @PostAuthorize("returnObject.assigneeMemberId.contains(authentication.principal.currMemberId)")
//    @PostFilter(value = "filterObject.assignedMemberId == authentication.principal.currMemberId")
    Task getByTaskId(String taskId) throws EntityNotFoundException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (result.isPresent()) {

            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    Task insertTask(Task task) {
        return taskRepository.save(task);
    }

    Task updateTask(String taskId, Task updateTask) throws EntityNotFoundException {
        Task task = getByTaskId(taskId);
        task.mergeForUpdate(updateTask);
        return taskRepository.save(task);
    }

    void deleteTask(String taskId) throws EntityNotFoundException {
        boolean exist = taskRepository.existsById(taskId);
        if (exist) {
            taskRepository.deleteById(taskId);
        } else {
            throw new EntityNotFoundException();
        }
    }

    boolean assignToTask(String taskId, List<String> memberIds) {
        //TODO check user before add
        return taskRepository.updateAssignee(taskId, memberIds);
    }
}
