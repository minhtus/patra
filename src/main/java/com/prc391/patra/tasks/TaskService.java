package com.prc391.patra.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    Optional<Task> getByTaskId(String taskId) {
        return taskRepository.findById(taskId);
    }

    Task insertTask(Task task) {
        return taskRepository.save(task);
    }

    Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    boolean deleteTask(String taskId) {
        boolean exist = taskRepository.existsById(taskId);
        if (exist) {
            taskRepository.deleteById(taskId);
            return true;
        } else {
            return false;
        }
    }
}
