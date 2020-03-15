package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final TaskRepository taskRepository;

    @Autowired
    public CommentService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    boolean comment(String taskId, Comment comment) throws EntityNotFoundException {
        if (!taskRepository.existsById(taskId)) {
            throw new EntityNotFoundException();
        }
        comment.setCommentId(new ObjectId().toString());
        return taskRepository.insertComment(taskId, comment);
    }

    boolean updateComment(String taskId, String commentId, Comment comment) throws EntityNotFoundException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            comment.setCommentId(commentId);
            return taskRepository.updateComment(taskId, comment);
        } else {
            throw new EntityNotFoundException();
        }
    }

    boolean deleteComment(String taskId, String commentId) throws EntityNotFoundException {
        if (taskRepository.existsById(taskId) && taskRepository.commentExist(taskId, commentId)) {
            return taskRepository.deleteComment(taskId, commentId);
        } else {
            throw new EntityNotFoundException();
        }
    }
}
