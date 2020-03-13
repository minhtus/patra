package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v0/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
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
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.insertTask(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@RequestBody Task task, @PathVariable("id") String taskId) {
        task.setTaskId(taskId);
        return ResponseEntity.ok(taskService.updateTask(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteTask(@PathVariable("id") String taskId) throws EntityNotFoundException {
        boolean result = taskService.deleteTask(taskId);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
