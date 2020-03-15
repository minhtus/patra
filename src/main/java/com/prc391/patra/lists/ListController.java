package com.prc391.patra.lists;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.lists.requests.CreateListRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lists")
public class ListController {

    private final ListService listService;
    private final ModelMapper mapper;

    @Autowired
    public ListController(ListService listService, ModelMapper mapper) {
        this.listService = listService;
        this.mapper = mapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<List> getList(@PathVariable("id") String listId) throws EntityNotFoundException {
        return ResponseEntity.ok(listService.getListById(listId));
    }

    @PostMapping
    public ResponseEntity<List> createList(@RequestBody CreateListRequest request) {
        return ResponseEntity.ok(listService.insertList(mapper.map(request, List.class)));
    }
}
