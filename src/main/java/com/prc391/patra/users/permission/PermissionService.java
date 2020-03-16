package com.prc391.patra.users.permission;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    @Autowired
    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission createPermission(Permission newPermission) throws EntityExistedException {
        Permission currPermission = permissionRepository.getByName(newPermission.getName());
        if (!ObjectUtils.isEmpty(currPermission)) {
            throw new EntityExistedException("Permission " + newPermission.getName() + " is existed!");
        }
        return permissionRepository.save(newPermission);
    }

    public Permission getPermission(Long id) throws EntityNotFoundException {
        Optional<Permission> permission = permissionRepository.findById(id);
        if (!permission.isPresent()) {
            throw new EntityNotFoundException("Permission " + id + " not exist!");
        }
        return permission.get();
    }

    public List<Permission> getAllPermission(){
        return permissionRepository.findAll();
    }
}
