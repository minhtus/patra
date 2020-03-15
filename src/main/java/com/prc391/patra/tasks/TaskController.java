package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.tasks.requests.CreateTaskRequest;
import com.prc391.patra.tasks.requests.UpdateTaskRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ModelMapper mapper;

    @Autowired
    public TaskController(TaskService taskService, ModelMapper mapper) {
        this.taskService = taskService;
        this.mapper = mapper;
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") String taskId) throws EntityNotFoundException {
        return ResponseEntity.ok(taskService.getByTaskId(taskId));
    }

    @PreAuthorize("hasAnyAuthority('WRITE')")
    @GetMapping("/test-write-premission")
    public ResponseEntity<String> testPreAuthorize() throws EntityNotFoundException {
        return ResponseEntity.ok("You writed!");
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.insertTask(mapper.map(request, Task.class)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@RequestBody UpdateTaskRequest request, @PathVariable("id") String taskId) throws EntityNotFoundException {
        return ResponseEntity.ok(taskService.updateTask(taskId, mapper.map(request, Task.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTask(@PathVariable("id") String taskId) throws EntityNotFoundException {
        taskService.deleteTask(taskId);
        return ResponseEntity.ok().build();
    }
}
