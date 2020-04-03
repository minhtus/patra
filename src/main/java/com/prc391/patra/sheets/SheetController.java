package com.prc391.patra.sheets;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.sheets.requests.CreateSheetRequest;
import com.prc391.patra.tasks.Task;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v0/sheets")
public class SheetController {

    private final SheetService sheetService;
    private final ModelMapper mapper;

    @Autowired
    public SheetController(SheetService sheetService, ModelMapper mapper) {
        this.sheetService = sheetService;
        this.mapper = mapper;
    }

    @GetMapping("/byOrg/{id}")
    public  ResponseEntity<List<Sheet>> getSheetByOrgID(@PathVariable("id") String orgID) throws EntityNotFoundException, UnauthorizedException {
        return ResponseEntity.ok(sheetService.getSheetFromOrgID(orgID));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sheet> getSheet(@PathVariable("id") String sheetId) throws EntityNotFoundException {
        return ResponseEntity.ok(sheetService.getSheetById(sheetId));
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<Task>> getTaskFromSheetId(
            @PathVariable("id") String sheetId) throws EntityNotFoundException {
        //let service return Task in order to use PostAuthorize
        return ResponseEntity.ok(sheetService.getTaskFromSheetId(sheetId));
    }

    @PostMapping
    public ResponseEntity<Sheet> createSheet(@RequestBody CreateSheetRequest request) {
        return ResponseEntity.ok(sheetService.insertSheet(mapper.map(request, Sheet.class)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteSheet(@PathVariable("id") String sheetId) {
        sheetService.deleteSheet(sheetId);
        return ResponseEntity.ok().build();
    }

}
