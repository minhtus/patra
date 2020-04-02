package com.prc391.patra.users.role;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.InvalidInputException;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//@RestController
//@RequestMapping("/v0/role")
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

//    @GetMapping
    public ResponseEntity<List<Role>> getAllRole() {
        return ResponseEntity.ok(roleService.getAllRole());
    }

//    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(
            @PathVariable(value = "id") Long roleId)
            throws EntityNotFoundException {
//        if (ObjectUtils.isEmpty(roleId)) {
//            return ResponseEntity.ok(roleService.getAllRole());
//        }
        //else
        return ResponseEntity.ok(roleService.getRoleById(roleId));
    }

//    @PostMapping
    public ResponseEntity<Role> createRole(
            @RequestBody Role request)
            throws EntityExistedException, EntityNotFoundException, InvalidInputException {
        return ResponseEntity.ok(roleService.insertRole(request));
    }

//    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(
            @RequestBody Role request,
            @PathVariable("id") Long roleId)
            throws EntityNotFoundException, InvalidInputException {
        return ResponseEntity.ok(roleService.updateRole(roleId, request));
    }
}
