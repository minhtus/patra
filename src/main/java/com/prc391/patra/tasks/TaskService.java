package com.prc391.patra.tasks;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.sheets.Sheet;
import com.prc391.patra.sheets.SheetRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.users.permission.PermissionRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.PatraStringUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final SheetRepository sheetRepository;
    private final UserRedisRepository userRedisRepository;
    private final UserRepository userRepository;
    private final AuthorizationUtils authorizationUtils;

    private static final Logger logger = Logger.getLogger("TaskServivce");

    Task getByTaskId(String taskId) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (result.isPresent()) {
            Task task = result.get();
            Sheet sheet = sheetRepository.findById(task.getSheetId()).get();
            if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.READ_ACCESS)) {
                throw new UnauthorizedException("You don't have permission to access this resource");
            }
            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    //TODO: delete this after done development
    public List<Map<String, Object>> getAllTask() {
        List<Task> getAll = taskRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : getAll) {
            Map<String, Object> map = new HashMap<>();
            map.put("taskId", task.getTaskId());
            map.put("taskName", task.getTaskName());
            map.put("taskDetail", task.getTaskDetails());
            List<Member> members = (List<Member>) memberRepository.findAllById(task.getAssignee() == null ? new ArrayList<>() : task.getAssignee());
            map.put("members", members.stream().map(member -> member.getUsername() + " " + member.getMemberId()));
            result.add(map);
        }
        return result;
    }

    public Task insertTask(Task task) throws EntityNotFoundException, UnauthorizedException {
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        Sheet sheet = optionalSheet.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        //write reporter MemberId
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
//        Optional<UserRedis> optionalUserRedis = userRedisRepository.findById(principal.getUsername());
//        UserRedis userRedis = optionalUserRedis.get();
        Member member = memberRepository.getByUsernameAndOrgId(principal.getUsername(), sheet.getOrgId());
        //save the task to get Task's id, so that reporter can be added into assignee list
        Task createdTask = taskRepository.save(task);
        createdTask.setReporter(member.getMemberId());
        createdTask.setAssignee(Arrays.asList(member.getMemberId()));
        memberRepository.updateAssignedTask(member.getMemberId(), Arrays.asList(createdTask.getTaskId()));
        return taskRepository.save(createdTask);
    }

    Task updateTask(String taskId, Task updateTask) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        Sheet sheet = optionalSheet.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)){
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member member = memberRepository.getByUsernameAndOrgId(principal.getUsername(), sheet.getOrgId());
        if (!task.getReporter().equals(member.getMemberId())) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        task.mergeForUpdate(updateTask);
        return taskRepository.save(task);
    }

    void deleteTask(String taskId) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException();
        }
        Task task = optionalTask.get();
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        Sheet sheet = optionalSheet.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)){
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member member = memberRepository.getByUsernameAndOrgId(principal.getUsername(), sheet.getOrgId());
        if (!task.getReporter().equals(member.getMemberId())) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        //remove assign information
        List<String> oldAssigneeIds = task.getAssignee();
        taskRepository.removeAssignee(taskId, oldAssigneeIds);
        memberRepository.removeAssignedTask(oldAssigneeIds, Arrays.asList(taskId));
        taskRepository.deleteById(taskId);
    }

    public Task changeTaskStatus(String taskId, RefTaskStatus status) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
        Sheet sheet = sheetRepository.findById(task.getSheetId()).get();

        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.WRITE_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        Member member = memberRepository.getByUsernameAndOrgId(ControllerSupportUtils.getPatraPrincipal().getUsername(), sheet.getOrgId());
        if (!task.getAssignee().contains(member.getMemberId())) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        task.setStatusId(status.getStatusId());
        taskRepository.save(task);
        return task;
    }

    boolean assignToTask(String taskId, List<String> requestedMemberIds)
            throws EntityNotFoundException, UnauthorizedException {
        //TODO check user before add
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
        Sheet sheet = sheetRepository.findById(task.getSheetId()).get();
        if (ObjectUtils.isEmpty(sheet)) {
            throw new EntityNotFoundException("Task does not belong to a sheet. Check the assignToTask() method in TaskService!");
        }
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        if (CollectionUtils.isEmpty(requestedMemberIds)) {
            requestedMemberIds = new ArrayList<>();
        }

        //validate each requestedMemberId:
        // - Member exist
        // - Member's Organization match with Sheet's Organization
        for (String requestedMemberId : requestedMemberIds) {
            Optional<Member> optionalMember = memberRepository.findById(requestedMemberId);
            //check member exist
            if (!optionalMember.isPresent()) {
                throw new EntityNotFoundException("Member with id " + requestedMemberId + " not exist!");
            }
            Member requestedMember = optionalMember.get();
            String requestedMemberOrgId = requestedMember.getOrgId();
            //check member's org match sheet's org
            if (!requestedMemberOrgId.equalsIgnoreCase(sheet.getOrgId())) {
//                throw new InvalidOrgException("Not valid organization");
                logger.log(Level.WARNING, "Requested Member's organization_id: " + requestedMemberId +
                        " is not match with Sheet's organization_id " + sheet.getOrgId());
                return false;
            }

            //check member's org vs user's current org
            //no need to check
        }
        //two way embedded, update two documents at the same time
        //remove all old assign information
        List<String> oldAssigneeIds = task.getAssignee();
        //remove all old assigneeList on Task
        boolean taskResult = taskRepository.removeAssignee(taskId, oldAssigneeIds);
        //remove all old assigned Task on old assigneeList
        boolean memberResult = memberRepository.removeAssignedTask(oldAssigneeIds, Arrays.asList(taskId));
        taskRepository.updateAssignee(taskId, requestedMemberIds);
        memberRepository.updateAssignedTaskMultipleUser(requestedMemberIds, Arrays.asList(taskId));
        return taskResult && memberResult;
    }
}
