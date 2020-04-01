package com.prc391.patra.tasks;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberService;
import com.prc391.patra.tasks.requests.CreateTaskRequest;
import com.prc391.patra.tasks.requests.UpdateTaskRequest;
import com.prc391.patra.users.UserRedisService;
import com.prc391.patra.users.permission.Permission;
import com.prc391.patra.users.permission.PermissionService;
import com.prc391.patra.utils.JWTUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v0/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ModelMapper mapper;
    private final UserRedisService userRedisService;
    private final PermissionService permissionService;
    private final MemberService memberService;

    @Autowired
    public TaskController(TaskService taskService, ModelMapper mapper, UserRedisService userRedisService, PermissionService permissionService, MemberService memberService) {
        this.taskService = taskService;
        this.mapper = mapper;
        this.userRedisService = userRedisService;
        this.permissionService = permissionService;
        this.memberService = memberService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable("id") String taskId) throws EntityNotFoundException, UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof PatraUserPrincipal) {
            PatraUserPrincipal principal = (PatraUserPrincipal) authentication.getPrincipal();
            String oldCurrMemberIdInRedis = userRedisService.getCurrMemberIdInRedis(principal.getUsername());
            if (!oldCurrMemberIdInRedis.equalsIgnoreCase(principal.getCurrMemberId())) {
                //redis and jwt is not sync
            }
            Task task = taskService.getByTaskId(taskId);
            String newCurrMemberIdInRedis = userRedisService.getCurrMemberIdInRedis(principal.getUsername());
            if (!oldCurrMemberIdInRedis.equalsIgnoreCase(newCurrMemberIdInRedis)) {
                HttpHeaders newAuthorizationHeader = getNewAuthorizationHeader(newCurrMemberIdInRedis, principal.getUsername());
                return ResponseEntity.ok()
                        .headers(newAuthorizationHeader)
                        .body(task);
            } else {
                return ResponseEntity.ok(task);
            }
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody CreateTaskRequest request) throws EntityNotFoundException {
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
        return ResponseEntity.ok(taskService.changeTaskStatus(taskId, status));
    }

    //TODO: delete this after development
    @GetMapping
    public ResponseEntity getAllTask() {
        return ResponseEntity.ok(taskService.getAllTask());
    }


    private HttpHeaders getNewAuthorizationHeader(String newCurrMemberIdInRedis, String username)
            throws EntityNotFoundException {
        Member member = memberService.getMember(newCurrMemberIdInRedis);
        //member here should not null, as memberservice had already throw exception when null
        List<String> newPermissions =
                permissionService.getPermission(Arrays.asList(member.getPermissions())).stream()
                        .map(permission -> permission.getName()).collect(Collectors.toList());
        String JWT = JWTUtils.buildJWT(newPermissions, newCurrMemberIdInRedis, username);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SecurityConstants.TOKEN_PREFIX + " " + JWT);
        return headers;
    }
}
