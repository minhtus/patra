package com.prc391.patra.tasks.impl;

import com.prc391.patra.tasks.Comment;
import com.prc391.patra.tasks.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public class CommentRepositoryImpl implements CommentRepository {
    private final MongoTemplate mongoTemplate;

    @Autowired
    public CommentRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean insertComment(String taskId, Comment comment) {
        return false;
    }
}
