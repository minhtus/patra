package com.prc391.patra.sheets;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.sheets.requests.CreateListRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/v0/lists")
public class SheetController {

    private final ListService listService;
    private final ModelMapper mapper;

    @Autowired
    public SheetController(ListService listService, ModelMapper mapper) {
        this.listService = listService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sheet> getList(@PathVariable("id") String listId) throws EntityNotFoundException {
        return ResponseEntity.ok(listService.getListById(listId));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<java.util.List<String>> getTaskFromListId(
            @PathVariable("id") String listId) throws EntityNotFoundException {
        //let service return Task in order to use PostAuthorize
        return ResponseEntity.ok(listService.getTaskFromListId(listId).stream()
                .map(task -> task.getTaskId()).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<Sheet> createList(@RequestBody CreateListRequest request) {
        return ResponseEntity.ok(listService.insertList(mapper.map(request, Sheet.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteList(@PathVariable("id") String listId) {
        listService.deleteList(listId);
        return ResponseEntity.ok().build();
    }

}
