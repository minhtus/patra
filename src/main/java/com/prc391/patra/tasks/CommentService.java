package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {
    private final TaskRepository taskRepository;

    @Autowired
    public CommentService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    boolean comment(String taskId, Comment comment) throws EntityNotFoundException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException();
        }
        Task commentTask = result.get();
        // comment
        taskRepository.insertComment(taskId, comment);
        return true;
    }
}
