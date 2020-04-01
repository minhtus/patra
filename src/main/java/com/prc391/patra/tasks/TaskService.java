package com.prc391.patra.tasks;

import com.prc391.patra.config.security.PatraUserPrincipal;
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
import com.prc391.patra.users.permission.Permission;
import com.prc391.patra.users.permission.PermissionRepository;
import com.prc391.patra.utils.PatraStringUtils;
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
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final SheetRepository sheetRepository;
    private final UserRedisRepository userRedisRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    private final Logger logger;

    @Autowired
    public TaskService(TaskRepository taskRepository, MemberRepository memberRepository, SheetRepository sheetRepository, UserRedisRepository userRedisRepository, UserRepository userRepository, PermissionRepository permissionRepository) {
        this.taskRepository = taskRepository;
        this.memberRepository = memberRepository;
        this.sheetRepository = sheetRepository;
        this.userRedisRepository = userRedisRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.logger = Logger.getLogger("TaskService");
    }

    //    @PostAuthorize("#Collections.contains(#Arrays.asList(#returnObject.assigneeMemberId), authentication.principal.currMemberId)")
    //    @PostFilter(value = "filterObject.assignedMemberId == authentication.principal.currMemberId")

    //    @PostAuthorize("returnObject.assignee.contains(authentication.principal.currMemberId)")
    Task getByTaskId(String taskId) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> result = taskRepository.findById(taskId);
        if (result.isPresent()) {
            if (!isAuthorized(result.get(), Arrays.asList("1"))) {
                throw new UnauthorizedException("Unauthorized");
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

    public Task insertTask(Task task) throws EntityNotFoundException {
        Optional<Sheet> optionalSheet = sheetRepository.findById(task.getSheetId());
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + task.getSheetId() + " not exist!");
        }
        return taskRepository.save(task);
    }

    Task updateTask(String taskId, Task updateTask) throws EntityNotFoundException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
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

    public Task changeTaskStatus(String taskId, RefTaskStatus status) throws EntityNotFoundException, UnauthorizedException {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }

        Task task = optionalTask.get();
        if (!isAuthorized(task, Arrays.asList("5"))) {
            throw new UnauthorizedException("Unauthorized");
        }
        task.setStatusId(status.getStatusId());
        taskRepository.save(task);
        return task;
    }

    boolean assignToTask(String taskId, List<String> requestedMemberIds)
            throws EntityNotFoundException {
        //TODO check user before add
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            throw new EntityNotFoundException("Task not exist");
        }
        Task task = optionalTask.get();
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

    /**
     * The method for authorization
     *
     * @param returnObject
     * @param method
     * @return
     */
    private boolean isAuthorized(Task returnObject, List<String> method) {
        if (ObjectUtils.isEmpty(returnObject)) {
            return false;
        }
        PatraUserPrincipal principal = getUserPrincipal();
        if (ObjectUtils.isEmpty(principal)) return false;
        //check permission
//        if (!permissionChecker(principal.getAuthorities())) return false;
        //check via currMemberIn JWT first
        String currMemberIdInJWT = principal.getCurrMemberId();
        if (checkWithCurrIdInJWT(currMemberIdInJWT, returnObject, method)) {
            return true;
        } else { //if currMember in JWT cannot access current Task, get all Member to check
            //get in redis first
            Optional<UserRedis> optionalUserRedis = userRedisRepository.findById(principal.getUsername());
            if (optionalUserRedis.isPresent()) {
                UserRedis userRedis = optionalUserRedis.get();
                if (checkWithUserRedis(userRedis, returnObject, method)) {
                    return true;
                }
            } else {//user not exist in redis, get in db
                Optional<User> optionalUser = userRepository.findById(principal.getUsername());
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    if (checkWithUserInDB(user, returnObject, method)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkWithCurrIdInJWT(String currMemberIdInJWT,
                                         Task task, List<String> mode) {
        Sheet sheet = getSheetFromId(task.getSheetId());
        if (ObjectUtils.isEmpty(sheet)) return false;
        if (!PatraStringUtils.isBlankAndEmpty(currMemberIdInJWT)) {
            Member currMember = getMemberFromId(currMemberIdInJWT);
            if (ObjectUtils.isEmpty(currMember)) {
                return false;
            }

            if (mode.contains("1")) {//get task
                if (sheet.getOrgId().equalsIgnoreCase(currMember.getOrgId())) {
                    return true;
                }
            }
            if (mode.contains("5")) {//set task done/undone
                //is assigned
                if (task.getAssignee().contains(currMemberIdInJWT)) {
                    //permission: MEMBER_EDIT, MEMBER_WRITE
                    List<String> permissions = permissionRepository.getByIdIn(Arrays.asList(currMember.getPermissions())).stream()
                            .map(permission -> permission.getName()).collect(Collectors.toList());
                    List<String> requiredPermissions = Arrays.asList("MEMBER_EDIT", "MEMBER_WRITE");
                    return permissionChecker(permissions, requiredPermissions);
                }
            }
        }
        return false;
    }

    private boolean checkWithUserRedis(UserRedis userRedis, Task task, List<String> mode) {
        Sheet sheet = getSheetFromId(task.getSheetId());
        if (ObjectUtils.isEmpty(sheet)) return false;
        List<String> memberIdsInRedis = userRedis.getMemberIds();
        if (CollectionUtils.isEmpty(memberIdsInRedis)) return false;
        for (String memberIdInRedis : memberIdsInRedis) {
            Member memberInRedis = getMemberFromId(memberIdInRedis);

            if (mode.contains("1")) {
                if (sheet.getOrgId().equalsIgnoreCase(memberInRedis.getOrgId())) {
                    setNewCurrMemberIdInRedis(userRedis, memberIdInRedis);
                    return true;
                }
            }
            if (mode.contains("5")) {
                //is assigned
                if (task.getAssignee().contains(memberIdInRedis)) {
                    //permission: MEMBER_READ, MEMBER_WRITE
                    List<String> permissions = permissionRepository.getByIdIn(Arrays.asList(memberInRedis.getPermissions())).stream()
                            .map(permission -> permission.getName()).collect(Collectors.toList());
                    List<String> requiredPermissions = Arrays.asList("MEMBER_EDIT", "MEMBER_WRITE");
                    if (permissionChecker(permissions, requiredPermissions)) {
                        setNewCurrMemberIdInRedis(userRedis, memberIdInRedis);
                        return true;
                    }
                }

            }
        }
        return false;
    }

    private boolean checkWithUserInDB(User user, Task task, List<String> mode) {
        Sheet sheet = getSheetFromId(task.getSheetId());
        if (ObjectUtils.isEmpty(sheet)) return false;
        List<Member> userMemberList = memberRepository.getAllByUsername(user.getUsername());
        if (CollectionUtils.isEmpty(userMemberList)) return false;
        for (Member userMember : userMemberList) {
            if (mode.contains("1")) {
                if (sheet.getOrgId().equalsIgnoreCase(userMember.getOrgId())) return true;
            }
            if (mode.contains("5")) {
                //is assigned
                if (task.getAssignee().contains(userMember)) {
                    //permission: MEMBER_READ, MEMBER_WRITE
                    List<String> permissions = permissionRepository.getByIdIn(Arrays.asList(userMember.getPermissions())).stream()
                            .map(permission -> permission.getName()).collect(Collectors.toList());
                    List<String> requiredPermissions = Arrays.asList("MEMBER_EDIT","MEMBER_WRITE");
                    if (permissionChecker(permissions, requiredPermissions)) return true;
                }

            }
        }
        return false;
    }

    private PatraUserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication)) {
            return null;
        }
        //get principal, to get username and currMemberIds
        Object principalObject = authentication.getPrincipal();
        if (!(principalObject instanceof PatraUserPrincipal)) {
            return null;
        }
        PatraUserPrincipal principal = (PatraUserPrincipal) principalObject;
        return principal;
    }

    private Sheet getSheetFromId(String id) {
        Optional<Sheet> optionalSheet = sheetRepository.findById(id);
        if (optionalSheet.isPresent()) {
            return optionalSheet.get();
        }
        return null;
    }

    private Member getMemberFromId(String id) {
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isPresent()) {
            return optionalMember.get();
        }
        return null;
    }

    private boolean permissionChecker(List<String> userPermissions, List<String> requiredPermissions) {
        if (CollectionUtils.isEmpty(userPermissions) || CollectionUtils.isEmpty(requiredPermissions)) {
            return false;
        }
        for (String requiredPermission : requiredPermissions) {
            if (userPermissions.contains(requiredPermission)) return true;
        }
        return false;
    }

    private void setNewCurrMemberIdInRedis(UserRedis userRedis, String newCurrMemberId) {
        userRedisRepository.deleteById(userRedis.getUsername());
        userRedis.setCurrMemberId(newCurrMemberId);
        userRedisRepository.save(userRedis);
    }
}
