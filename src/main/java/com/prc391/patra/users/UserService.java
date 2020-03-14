package com.prc391.patra.users;

import com.prc391.patra.users.role.Role;
import com.prc391.patra.users.role.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User registerUser(User newUserInfo) {
        if (ObjectUtils.isEmpty(newUserInfo)) {
            //throw exception here (if necessary)
            return null;
        }
        User userInDB = userRepository.getUserByUsername(newUserInfo.getUsername());
        if (!ObjectUtils.isEmpty(userInDB)) {
            //TODO: throw new User Already Existed Exception
            return null;
        }

        User user = new User();
        user.setUsername(newUserInfo.getUsername());
        user.setPassHash(passwordEncoder.encode(newUserInfo.getPassHash()));
        user.setName(newUserInfo.getName());
        //TODO: implement email verification, if possible
        user.setEmail(newUserInfo.getEmail());

        user.setEnabled(newUserInfo.isEnabled());
        List<Long> userRoles = new ArrayList<>();
        for (Long roleId : newUserInfo.getRoles()) {
            Optional<Role> currentRole = roleRepository.findById(roleId);
            if (currentRole.isPresent()) {
                userRoles.add(roleId);
            } else {
                //TODO: role is not present
            }
        }
        user.setRoles(userRoles);

        //Do you... really waaaaant to return the hashed password?
        //The password also contains the encoding algorithm, good luck
        return userRepository.save(user);
    }

    User getUser(String username) {
        return userRepository.getUserByUsername(username);
    }

}
