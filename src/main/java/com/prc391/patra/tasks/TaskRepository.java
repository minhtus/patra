package com.prc391.patra.tasks;

import org.springframework.data.mongodb.repository.MongoRepository;

interface TaskRepository extends MongoRepository<Task, String> {
}
