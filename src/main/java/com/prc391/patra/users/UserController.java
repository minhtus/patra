package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.users.requests.CreateUserRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v0/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;

    @Autowired
    public UserController(UserService userService, ModelMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username) throws EntityNotFoundException {
        //TODO get by username or email
        return ResponseEntity.ok(userService.getUser(username));
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody CreateUserRequest request) throws EntityExistedException {
        return ResponseEntity.ok(userService.registerUser(mapper.map(request, User.class)));
    }

    //TODO get all org of user (members) maybe done?
    //TODO get all lists of user
    //TODO get all task of user
}
