package com.prc391.patra.tasks;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String>, CommentRepository, TaskRepositoryCustom {
    List<Task> getAllBySheetId(String listIds);
}
