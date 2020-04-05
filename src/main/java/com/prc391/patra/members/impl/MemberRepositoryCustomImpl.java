package com.prc391.patra.members.impl;

import com.mongodb.client.result.UpdateResult;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepositoryCustom;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public MemberRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean updateAssignedTask(String memberId, List<String> taskIds) {
        UpdateResult result = mongoTemplate.updateFirst(query(where("_id").is(memberId)),
                new Update().addToSet("assignedTaskId").each(taskIds), Member.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }

    @Override
    public boolean updateAssignedTaskMultipleUser(List<String> memberIds, List<String> taskIds) {
        UpdateResult result = mongoTemplate.updateMulti(query(where("_id").in(memberIds)),
                new Update().addToSet("assignedTaskId").each(taskIds), Member.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }

    @Override
    public boolean removeAssignedTask(List<String> memberIds, List<String> taskIds) {
        UpdateResult result = mongoTemplate.updateMulti(query(where("_id").in(memberIds)),
                new Update().pullAll("assignedTaskId",taskIds.toArray()), Member.class);
        return result.wasAcknowledged() && result.getModifiedCount() > 0;
    }


}
