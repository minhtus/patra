package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.users.role.RoleRepository;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper mapper;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.mapper = mapper;
    }

    public User registerUser(User newUserInfo) {
        if (ObjectUtils.isEmpty(newUserInfo)) {
            //throw exception here (if necessary)
            return null;
        }
//        User userInDB = userRepository.getUserByUsername(newUserInfo.getUsername());
        Optional<User> userInDB = userRepository.findById(newUserInfo.getId());
        if (userInDB.isPresent()) {
            //TODO: throw new User Already Existed Exception
            return null;
        }
        User user = new User();
        mapper.map(newUserInfo, user);

//        user.setUsername(newUserInfo.getUsername());
//        user.setPassHash(passwordEncoder.encode(newUserInfo.getPassHash()));
//        user.setName(newUserInfo.getName());
        //TODO: implement email verification, if possible
//        user.setEmail(newUserInfo.getEmail());

//        user.setEnabled(newUserInfo.isEnabled());
//        List<Long> userRoles = new ArrayList<>();
//        for (Long roleId : newUserInfo.getRoles()) {
//            Optional<Role> currentRole = roleRepository.findById(roleId);
//            if (currentRole.isPresent()) {
//                userRoles.add(roleId);
//            } else {
//                //TODO: role is not present
//            }
//        }
//        user.setRoles(userRoles);
        return userRepository.save(user);
    }

    User getUser(String username) throws EntityNotFoundException {
//        return userRepository.getUserByUsername(username);
        Optional<User> user = userRepository.findById(username);
        if (!user.isPresent()) {
            throw new EntityNotFoundException("User "+ username +" not found");
        }
        return user.get();
    }

}
