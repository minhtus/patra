package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
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

    Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    boolean deleteTask(String taskId) throws EntityNotFoundException {
        boolean exist = taskRepository.existsById(taskId);
        if (exist) {
            taskRepository.deleteById(taskId);
            return true;
        } else {
            throw new EntityNotFoundException();
        }
    }
}
