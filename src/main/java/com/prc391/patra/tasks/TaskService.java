package com.prc391.patra.tasks;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.sheets.Sheet;
import com.prc391.patra.sheets.SheetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final SheetRepository sheetRepository;

    private final Logger logger;

    @Autowired
    public TaskService(TaskRepository taskRepository, MemberRepository memberRepository, SheetRepository sheetRepository) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.sheetRepository = sheetRepository;
        this.logger = Logger.getLogger("TaskService");
    }

    //    @PostAuthorize("#Collections.contains(#Arrays.asList(#returnObject.assigneeMemberId), authentication.principal.currMemberId)")
    //    @PostFilter(value = "filterObject.assignedMemberId == authentication.principal.currMemberId")

    //    @PostAuthorize("returnObject.assignee.contains(authentication.principal.currMemberId)")
    Task getByTaskId(String taskId) throws EntityNotFoundException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (result.isPresent()) {

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

    public Task insertTask(Task task) throws EntityNotFoundException {
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        return taskRepository.save(task);
    }

    Task updateTask(String taskId, Task updateTask) throws EntityNotFoundException {
        Task task = getByTaskId(taskId);
        task.mergeForUpdate(updateTask);
        return taskRepository.save(task);
    }

    void deleteTask(String taskId) throws EntityNotFoundException {
        boolean exist = taskRepository.existsById(taskId);
        if (exist) {
            taskRepository.deleteById(taskId);
        } else {
            throw new EntityNotFoundException();
        }
    }

    boolean assignToTask(String taskId, List<String> requestedMemberIds)
            throws EntityNotFoundException {
        //TODO check user before add
        Task task = this.getByTaskId(taskId);
        Sheet sheet = sheetRepository.findById(task.getSheetId()).get();
        if (ObjectUtils.isEmpty(sheet)) {
            throw new EntityNotFoundException("Task does not belong to a sheet??? Check the assignToTask() method in TaskService!");
        }
        if (CollectionUtils.isEmpty(requestedMemberIds)) {
            requestedMemberIds = new ArrayList<>();
        }
        //get currentMember's Member entity
        //can't use PreAuthorize and PostAuthorize because the param does not contains
        //org info, the return value is boolean which does not contain org info,
        //need to get org info from repository, which Pre/PostAuthorize cannot do
        //check in code instead
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
            //less likely to happen
            logger.log(Level.WARNING, "Authentication is empty");
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof PatraUserPrincipal)) {
            logger.log(Level.WARNING, "AnonymousUser!");
            return false;
        }
        Optional<Member> currMemberOptional = memberRepository.findById(((PatraUserPrincipal) authentication.getPrincipal()).getCurrMemberId());
        if (!currMemberOptional.isPresent()) {
            logger.log(Level.WARNING, "Current member does not exist in db!");
            throw new EntityNotFoundException("Current member does not exist in db!");

        }
        //end get currMember's Member entity

        //check currMember's orgId vs sheet's orgId
        if (!currMemberOptional.get().getOrgId().equalsIgnoreCase(sheet.getOrgId())) {
            // throw new ForbiddenException()
            logger.log(Level.WARNING, "Current Member's organization_id: "
                    + currMemberOptional.get().getOrgId() +
                    " is not match with Sheet's organization_id: " + sheet.getOrgId());
            return false;
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

//    boolean unassignToTask(String taskId, List<String> requestedMemberIds) throws EntityNotFoundException {
//        Task task = this.getByTaskId(taskId);
//        Sheet sheet = sheetRepository.findById(task.getSheetId()).get();
//        if (ObjectUtils.isEmpty(sheet)) {
//            throw new EntityNotFoundException("Task does not belong to a sheet??? Check the assignToTask() method in TaskService!");
//        }
//
//        //test member's permission before unassigning
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (ObjectUtils.isEmpty(authentication)) {
//            //less likely to happen
//            logger.log(Level.WARNING, "Authentication is empty");
//            return false;
//        }
//        Object principal = authentication.getPrincipal();
//        if (!(principal instanceof PatraUserPrincipal)) {
//            logger.log(Level.WARNING, "AnonymousUser!");
//            return false;
//        }
//        Optional<Member> currMemberOptional = memberRepository.findById(((PatraUserPrincipal) authentication.getPrincipal()).getCurrMemberId());
//        if (!currMemberOptional.isPresent()) {
//            logger.log(Level.WARNING, "Current member does not exist in db!");
//            throw new EntityNotFoundException("Current member does not exist in db!");
//
//        }
//
//        if (!currMemberOptional.get().getOrgId().equalsIgnoreCase(sheet.getOrgId())) {
//            // throw new ForbiddenException()
//            logger.log(Level.WARNING, "Current Member's organization_id: "
//                    + currMemberOptional.get().getOrgId() +
//                    " is not match with Sheet's organization_id: " + sheet.getOrgId());
//            return false;
//        }
//        //end checking member's permission
//
//        //check if task does have any assignee
//        if (CollectionUtils.isEmpty(task.getAssignee())) {
//            throw new EntityNotFoundException("Task " + taskId + " does not have any assignee!");
//        }
//        //use this list to ignore requestedMemberIds that do not assigned to Task.
//        // Add not-assigned memberIds into this list and remove from requestedMemberIds at the end of the loop
//        List<String> invalidAssigneeId = new ArrayList<>();
//
//        //validate each requestedMemberId:
//        // - Member exist
//        // - Member's id is exist in Task, Task's id is exist in Member
//        for (String requestedMemberId : requestedMemberIds) {
//            Optional<Member> optionalMember = memberRepository.findById(requestedMemberId);
//            //check member exist
//            if (!optionalMember.isPresent()) {
//                throw new EntityNotFoundException("Member with id " + requestedMemberId + " not exist!");
//            }
//            Member member = optionalMember.get();
//            //check if member is assigned to any task
//            if (CollectionUtils.isEmpty(member.getAssignedTaskId())) {
////                throw new EntityNotFoundException("Member " + requestedMemberId + " is not assigned to any task!");
//                logger.log(Level.INFO, "Member " + requestedMemberId + " is not assigned to any task!");
//
//            }
//            if (!member.getAssignedTaskId().contains(taskId)) {
////                throw new EntityNotFoundException("Member with id " + requestedMemberId +
////                        " doesn't have task " + taskId);
//                logger.log(Level.INFO, "Member with id " + requestedMemberId +
//                        " doesn't have task " + taskId);
//                invalidAssigneeId.add(requestedMemberId);
//            }
//
//            if (!task.getAssignee().contains(requestedMemberId)) {
////                throw new EntityNotFoundException("Task " + taskId + " does not assigned to member " + requestedMemberId);
//                logger.log(Level.INFO, "Task " + taskId + " does not assigned to member " + requestedMemberId);
//                if (!invalidAssigneeId.contains(requestedMemberId))
//                    invalidAssigneeId.add(requestedMemberId);
//            }
//        }
//        requestedMemberIds.removeAll(invalidAssigneeId);
//        return taskRepository.removeAssignee(taskId, requestedMemberIds) &&
//                memberRepository.removeAssignedTask(requestedMemberIds, Arrays.asList(taskId));
//    }
}
