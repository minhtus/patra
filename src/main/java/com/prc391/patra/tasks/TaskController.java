package com.prc391.patra.tasks;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.members.MemberService;
import com.prc391.patra.tasks.requests.CreateTaskRequest;
import com.prc391.patra.tasks.requests.UpdateTaskRequest;
import com.prc391.patra.users.UserRedisService;
import com.prc391.patra.users.permission.PermissionService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v0/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ModelMapper mapper;
    private final UserRedisService userRedisService;
    private final PermissionService permissionService;
    private final MemberService memberService;
    private final JwtRedisService jwtRedisService;

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") String taskId, @AuthenticationPrincipal Jwt principle) throws EntityNotFoundException, UnauthorizedException {
        Task task = taskService.getByTaskId(taskId);
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) throws EntityNotFoundException, UnauthorizedException {
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

    @PatchMapping("/{id}/assignees")
    public ResponseEntity assignTask(@PathVariable("id") String taskId,
                                     @RequestParam(required = false) List<String> memberIds)
            throws EntityNotFoundException {
        boolean result = taskService.assignToTask(taskId, memberIds);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity setTaskStatus(@PathVariable("id") String taskId,
                                        @RequestParam RefTaskStatus status) throws EntityNotFoundException, UnauthorizedException {
        Task task = taskService.changeTaskStatus(taskId, status);
            return ResponseEntity.ok(task);
    }

    //TODO: delete this after development
    @GetMapping
    public ResponseEntity getAllTask() {
        return ResponseEntity.ok(taskService.getAllTask());
    }

}
