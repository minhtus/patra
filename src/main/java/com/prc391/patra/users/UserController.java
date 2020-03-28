package com.prc391.patra.users;

import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.users.requests.CreateUserRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public ResponseEntity<User> getUserByUsername(
            @PathVariable(value = "username") String username
    ) throws EntityNotFoundException {
        //TODO get by username or email
        return ResponseEntity.ok(userService.getUser(username));
    }

    //needed this, otherwise it will call the POST method below
    @GetMapping
    public ResponseEntity<User> getUserByUsername() throws EntityNotFoundException {
        //TODO get by username or email
        return ResponseEntity.ok(userService.getUser(null));
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody CreateUserRequest request) throws EntityExistedException, EntityNotFoundException {
        User user = mapper.map(request, User.class);
        user.setPassHash(request.getPassword());
        return ResponseEntity.ok(userService.registerUser(user));
    }

    //TODO get all org of user (members) maybe done?
    @GetMapping("/{username}/organization")
    public ResponseEntity<List<Organization>> getUserOrganization(
            @PathVariable("username") String username) throws EntityNotFoundException {
        return ResponseEntity.ok(userService.getUserOrganization(username));
    }

    //TODO get all lists of user
    //TODO get all task of user
}
