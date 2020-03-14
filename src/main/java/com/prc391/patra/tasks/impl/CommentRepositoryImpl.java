package com.prc391.patra.tasks.impl;

import com.mongodb.client.result.UpdateResult;
import com.prc391.patra.tasks.Comment;
import com.prc391.patra.tasks.CommentRepository;
import com.prc391.patra.tasks.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import static org.springframework.data.mongodb.core.query.Query.*;
import static org.springframework.data.mongodb.core.query.Criteria.*;

import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CommentRepositoryImpl implements CommentRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public CommentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean insertComment(String taskId, Comment comment) {
        UpdateResult result = mongoTemplate.updateFirst(query(where("_id").is(taskId)),
                new Update().push("comments", comment), Task.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }

    @Override
    public boolean commentExist(String taskId, String commentId) {
        return mongoTemplate.exists(query(where("_id").is(taskId).and("comments.comment_id").is(commentId)), Task.class);
    }

    public boolean updateComment(String taskId, Comment comment) {
        UpdateResult result = mongoTemplate.updateFirst(query(where("_id").is(taskId)
                        .and("comments.comment_id").is(comment.getCommentId())),
                new Update().set("comments.$", comment), Task.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }

    @Override
    public boolean deleteComment(String taskId, String commentId) {
        UpdateResult result = mongoTemplate.updateFirst(query(where("_id").is(taskId)),
                new Update().pull("comments", query(where("comment_id").is(commentId))), Task.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }
}
