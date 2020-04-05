package com.prc391.patra.sheets;

import com.prc391.patra.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.tasks.Task;
import com.prc391.patra.tasks.TaskRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import com.prc391.patra.utils.ControllerSupportUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SheetService {
    private final SheetRepository sheetRepository;
    private final TaskRepository taskRepository;
    private final AuthorizationUtils authorizationUtils;
    private final MemberRepository memberRepository;

    public List<Sheet> getSheetFromOrgID(String orgID) throws EntityNotFoundException, UnauthorizedException {
        if (authorizationUtils.authorizeAccess(orgID, SecurityConstants.READ_ACCESS)) {
            List<Sheet> result = sheetRepository.getAllByOrgIdIn(orgID);
            if (CollectionUtils.isEmpty(result)){
                throw new EntityNotFoundException();
            }
            return result;
        } else {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
    }

    Sheet getSheetById(String sheetId) throws EntityNotFoundException, UnauthorizedException {
        Optional<Sheet> result = sheetRepository.findById(sheetId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException("Sheet " + sheetId + " not exist");
        }
        Sheet sheet = result.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.READ_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        return sheet;
    }

    public List<Task> getTaskFromSheetId(String sheetId) throws EntityNotFoundException, UnauthorizedException {
        Optional<Sheet> result = sheetRepository.findById(sheetId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + sheetId + " is not exist!");
        }
        Sheet sheet = result.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.READ_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        List<Task> taskList = taskRepository.getAllBySheetId(sheetId);
        return taskList;
    }

    Sheet insertSheet(Sheet sheet) throws UnauthorizedException, EntityNotFoundException, EntityExistedException {
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
//        Optional<Sheet> optionalSheet = sheetRepository.findById(sheet.getSheetId());
//        if (optionalSheet.isPresent()) {
//            throw new EntityExistedException("Sheet " + sheet.getSheetId() + " is existed");
//        }
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        Member member = memberRepository.getByUsernameAndOrgId(principal.getUsername(), sheet.getOrgId());
        sheet.setReporter(member.getMemberId());
        return sheetRepository.save(sheet);
    }

    void deleteSheet(String sheetId) throws EntityNotFoundException, UnauthorizedException, EntityExistedException {
        Optional<Sheet> optionalSheet = sheetRepository.findById(sheetId);
        if (!optionalSheet.isPresent()) {
            throw new EntityNotFoundException("Sheet " + sheetId + " is not exist");
        }
        Sheet sheet = optionalSheet.get();
        if (!authorizationUtils.authorizeAccess(sheet.getOrgId(), SecurityConstants.ADMIN_ACCESS)) {
            throw new UnauthorizedException("You don't have permission to access this resource");
        }
        List<Task> taskInSheet = taskRepository.getAllBySheetId(sheetId);
        for (Task task : taskInSheet) {
            //remove assign information
            List<String> oldAssigneeIds = task.getAssignee();
            taskRepository.removeAssignee(task.getTaskId(), oldAssigneeIds);
            memberRepository.removeAssignedTask(oldAssigneeIds, Arrays.asList(task.getTaskId()));
        }
        taskRepository.deleteAll(taskInSheet);
        sheetRepository.deleteById(sheetId);
    }
}
