package com.prc391.patra.tasks;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.lists.ListRepository;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ListRepository listRepository;

    private final Logger logger;

    @Autowired
    public TaskService(TaskRepository taskRepository, MemberRepository memberRepository, ListRepository listRepository) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.listRepository = listRepository;
        this.logger = Logger.getLogger("TaskService");
    }

    //    @PostAuthorize("#Collections.contains(#Arrays.asList(#returnObject.assigneeMemberId), authentication.principal.currMemberId)")
    @PostAuthorize("returnObject.assignee.contains(authentication.principal.currMemberId)")
//    @PostFilter(value = "filterObject.assignedMemberId == authentication.principal.currMemberId")
    Task getByTaskId(String taskId) throws EntityNotFoundException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (result.isPresent()) {

            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    Task insertTask(Task task) {
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
        com.prc391.patra.lists.List list = listRepository.findById(task.getListId()).get();
        if (ObjectUtils.isEmpty(list)) {
            throw new EntityNotFoundException("Task does not belong to a list??? Check the assignToTask() method in TaskService!");
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

        //check currMember's orgId vs list's orgId
        if (!currMemberOptional.get().getOrgId().equalsIgnoreCase(list.getOrgId())) {
            // throw new ForbiddenException()
            logger.log(Level.WARNING, "Current Member's organization_id: "
                    + currMemberOptional.get().getOrgId() +
                    " is not match with List's organization_id: " + list.getOrgId());
            return false;
        }

        //validate each requestedMemberId:
        // - Member exist
        // - Member's Organization match with List's Organization
        for (String requestedMemberId : requestedMemberIds) {
            Optional<Member> optionalMember = memberRepository.findById(requestedMemberId);
            //check member exist
            if (!optionalMember.isPresent()) {
                throw new EntityNotFoundException("Member with id " + requestedMemberId + " not exist!");
            }
            Member requestedMember = optionalMember.get();
            String requestedMemberOrgId = requestedMember.getOrgId();
            //check member's org match list's org
            if (!requestedMemberOrgId.equalsIgnoreCase(list.getOrgId())) {
//                throw new InvalidOrgException("Not valid organization");
                logger.log(Level.WARNING, "Requested Member's organization_id: " + requestedMemberId +
                        " is not match with List's organization_id " + list.getOrgId());
                return false;
            }

            //check member's org vs user's current org
            //no need to check
        }
        //two way embedded, update two documents at the same time
        return taskRepository.updateAssignee(taskId, requestedMemberIds)
                && memberRepository.updateAssignedTaskMultipleUser(requestedMemberIds, Arrays.asList(taskId));
    }
}
