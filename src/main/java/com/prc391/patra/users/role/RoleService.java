package com.prc391.patra.users.role;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.InvalidInputException;
import com.prc391.patra.users.permission.Permission;
import com.prc391.patra.users.permission.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public List<Role> getAllRole() {
        return roleRepository.findAll();
    }

    public Role getRoleById(Long id) throws EntityNotFoundException {
        Optional<Role> roleInDB = roleRepository.findById(id);
        if (!roleInDB.isPresent()) {
            throw new EntityNotFoundException();
        }
        return roleInDB.get();
    }

    public Role insertRole(Role newRole) throws
            EntityExistedException, EntityNotFoundException, InvalidInputException {
        Optional<Role> currRole = roleRepository.findById(newRole.getId());
        if (currRole.isPresent()) {
            throw new EntityExistedException(
                    "Role " + currRole.get().getId() + "(" + currRole.get().getName() + ") is existed!");
        }
        //else
        checkPermissionIdListForExistence(newRole.getPermissions());
        return roleRepository.save(newRole);
    }

    //updateRole: is dangerous, just update permissions associated with role
    public Role updateRole(Long roleId, Role role) throws
            EntityNotFoundException, InvalidInputException {
        Optional<Role> currRole = roleRepository.findById(roleId);
        if (!currRole.isPresent()) {
            throw new EntityNotFoundException("Role " + roleId + " is not exist!");
        }
        //else
        checkPermissionIdListForExistence(role.getPermissions());
        Role updateRole = currRole.get();
        updateRole.setPermissions(role.getPermissions());
        return roleRepository.save(updateRole);
    }

    //deleteROle: is dangerous too, must check if any user still have the "being" deleted role
    //NOT_IMPLEMENTED
    private boolean deleteRole() {
        return false;
    }

    private void checkPermissionIdListForExistence(List<Long> permissionList) throws InvalidInputException, EntityNotFoundException {
        if (CollectionUtils.isEmpty(permissionList)) {
            throw new InvalidInputException();
        }
        //else
        //check if permission is exist
        //throw exception with list of invalid permission id
        List<Long> notExistPermissionIdList = new ArrayList<>();
        for (Long permissionId : permissionList) {
            Optional<Permission> currPermission = permissionRepository.findById(permissionId);
            if (!currPermission.isPresent()) {
                notExistPermissionIdList.add(permissionId);
            }
        }
        if (!notExistPermissionIdList.isEmpty()) {
            throw new EntityNotFoundException(
                    "Permissions with ids not exist: " + notExistPermissionIdList.toString());
        }
        //end check if permission is exist
    }
}
