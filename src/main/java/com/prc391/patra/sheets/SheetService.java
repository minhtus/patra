package com.prc391.patra.sheets;

import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.tasks.Task;
import com.prc391.patra.tasks.TaskRepository;
import com.prc391.patra.utils.AuthorizationUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SheetService {
    private final SheetRepository sheetRepository;
    private final TaskRepository taskRepository;
    private final AuthorizationUtils authorizationUtils;


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

    Sheet getSheetById(String sheetId) throws EntityNotFoundException {
        Optional<Sheet> result = sheetRepository.findById(sheetId);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    public List<Task> getTaskFromSheetId(String sheetId) throws EntityNotFoundException {
        Optional<Sheet> result = sheetRepository.findById(sheetId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + sheetId + " is not exist!");
        }
        List<Task> taskList = taskRepository.getAllBySheetId(sheetId);
        return taskList;
    }

    Sheet insertSheet(Sheet sheet) {
        return sheetRepository.save(sheet);
    }

    void deleteSheet(String sheetId) {
        //TODO check permission
        sheetRepository.deleteById(sheetId);
    }
}
