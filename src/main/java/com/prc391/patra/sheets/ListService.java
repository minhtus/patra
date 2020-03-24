package com.prc391.patra.sheets;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.tasks.Task;
import com.prc391.patra.tasks.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ListService {
    private final ListRepository listRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ListService(ListRepository listRepository, TaskRepository taskRepository) {
        this.listRepository = listRepository;
        this.taskRepository = taskRepository;
    }

    Sheet getListById(String listId) throws EntityNotFoundException {
        Optional<Sheet> result = listRepository.findById(listId);
        if (result.isPresent()) {
            return result.get();
        } else {
            throw new EntityNotFoundException();
        }
    }

    public java.util.List<Task> getTaskFromListId(String listId) throws EntityNotFoundException {
        Optional<Sheet> result = listRepository.findById(listId);
        if (!result.isPresent()) {
            throw new EntityNotFoundException("Sheet with id " + listId + " is not exist!");
        }
        java.util.List<Task> taskList = taskRepository.getAllByListId(listId);
        return taskList;
    }

    Sheet insertList(Sheet sheet) {
        return listRepository.save(sheet);
    }

    void deleteList(String listId) {
        //TODO check permission
        listRepository.deleteById(listId);
    }
}
