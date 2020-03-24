package com.prc391.patra.users.permission;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v0/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/{id}")
    //@PreAuthorize("")
    public ResponseEntity<Permission> getPermission
            (@PathVariable(name = "id") Long id) throws EntityNotFoundException {
        return ResponseEntity.ok(permissionService.getPermission(id));
    }

    @GetMapping()
    //@PreAuthorize("")
    public ResponseEntity<List<Permission>> getAllPermission() {
        return ResponseEntity.ok(permissionService.getAllPermission());
    }

    @PostMapping
    //@PreAuthorize("")
    public ResponseEntity<Permission> createPermission(
            @RequestBody Permission permission
    ) throws EntityExistedException {
        return ResponseEntity.ok(permissionService.createPermission(permission));
    }
}
