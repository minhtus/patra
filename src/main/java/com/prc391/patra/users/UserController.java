package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.users.request.UserResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v0/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final ModelMapper mapper;

    @Autowired
    public UserController(UserService userService, AuthenticationService authenticationService, ModelMapper mapper) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.mapper = mapper;
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable("username") String username) throws EntityNotFoundException {
        return ResponseEntity.ok(mapper.map(userService.getUser(username), UserResponse.class));
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody User user) {
        return ResponseEntity.ok(mapper.map(userService.registerUser(user), UserResponse.class));
    }
}
