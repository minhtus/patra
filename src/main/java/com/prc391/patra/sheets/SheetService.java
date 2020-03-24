package com.prc391.patra.sheets;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.tasks.Task;
import com.prc391.patra.tasks.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SheetService {
    private final SheetRepository sheetRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public SheetService(SheetRepository sheetRepository, TaskRepository taskRepository) {
        this.sheetRepository = sheetRepository;
        this.taskRepository = taskRepository;
    }

    Sheet getListById(String listId) throws EntityNotFoundException {
        Optional<Sheet> result = sheetRepository.findById(listId);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    public java.util.List<Task> getTaskFromListId(String listId) throws EntityNotFoundException {
        Optional<Sheet> result = sheetRepository.findById(listId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + listId + " is not exist!");
        }
        java.util.List<Task> taskList = taskRepository.getAllByListId(listId);
        return taskList;
    }

    Sheet insertList(Sheet sheet) {
        return sheetRepository.save(sheet);
    }

    void deleteList(String listId) {
        //TODO check permission
        sheetRepository.deleteById(listId);
    }
}
