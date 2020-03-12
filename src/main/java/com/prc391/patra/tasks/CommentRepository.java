package com.prc391.patra.tasks;

public interface CommentRepository {
    boolean insertComment(String taskId, Comment comment);
}
