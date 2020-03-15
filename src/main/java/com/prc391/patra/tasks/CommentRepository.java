package com.prc391.patra.tasks;

public interface CommentRepository {
    boolean insertComment(String taskId, Comment comment);
    boolean commentExist(String taskId, String commentId);
    boolean updateComment(String taskId, Comment comment);
    boolean deleteComment(String taskId, String commentId);
}
