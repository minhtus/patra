package com.prc391.patra.tasks.impl;

import com.mongodb.client.result.UpdateResult;
import com.prc391.patra.tasks.Task;
import com.prc391.patra.tasks.TaskRepositoryCustom;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class TaskRepositoryCustomImpl implements TaskRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public TaskRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean updateAssignee(String taskId, List<String> memberIds) {
        UpdateResult result = mongoTemplate.updateFirst(query(where("_id").is(taskId)),
                new Update().addToSet("assignee").each(memberIds), Task.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }
}
