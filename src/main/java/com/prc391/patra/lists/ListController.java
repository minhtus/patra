package com.prc391.patra.lists;

import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lists")
public class ListController {

    private final ListService listService;

    @Autowired
    public ListController(ListService listService) {
        this.listService = listService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List> getList(@PathVariable("id") String listId) throws EntityNotFoundException {
        return ResponseEntity.ok(listService.getListById(listId));
    }

    @PostMapping
    public ResponseEntity<List> createList(@RequestBody List list) {
        return ResponseEntity.ok(listService.insertNewList(list));
    }
}
