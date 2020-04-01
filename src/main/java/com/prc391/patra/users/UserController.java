package com.prc391.patra.users;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.InvalidInputException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberService;
import com.prc391.patra.members.responses.MemberResponse;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.users.permission.PermissionService;
import com.prc391.patra.users.requests.CreateUserRequest;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.JWTUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v0/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;
    private final MemberService memberService;
    private final PermissionService permissionService;

    @Autowired
    public UserController(UserService userService, ModelMapper mapper, MemberService memberService, PermissionService permissionService) {
        this.userService = userService;
        this.mapper = mapper;
        this.memberService = memberService;
        this.permissionService = permissionService;
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
    public ResponseEntity<User> registerUser(@RequestBody CreateUserRequest request) throws EntityExistedException {
        User user = mapper.map(request, User.class);
        user.setPassHash(request.getPassword());
        return ResponseEntity.ok(userService.registerUser(user));
    }

    //TODO get all org of user (members) maybe done?
    @GetMapping("/{username}/organizations")
    public ResponseEntity<List<Organization>> getUserOrganization(
            @PathVariable("username") String username) throws EntityNotFoundException {
        return ResponseEntity.ok(userService.getUserOrganization(username));
    }

    @GetMapping("/{username}/members")
    public ResponseEntity<List<MemberResponse>> getUserMember(
            @PathVariable("username") String username) throws EntityNotFoundException {
        return ResponseEntity.ok(userService.getUserMember(username));
    }

    @PatchMapping("/{username}/curr-member")
    public ResponseEntity updateCurrMemberId(
            @PathVariable("username") String username,
            @RequestParam("currMember") String currMemberId
    ) throws EntityNotFoundException, InvalidInputException, UnauthorizedException {
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        if (!username.equalsIgnoreCase(principal.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.updateCurrMemberId(username, currMemberId);
        HttpHeaders newHeader = getNewAuthorizationHeader(currMemberId, principal.getUsername());
        return ResponseEntity.ok().headers(newHeader).build();
    }



    //TODO get all lists of user
    //TODO get all task of user

    private HttpHeaders getNewAuthorizationHeader(String newCurrMemberIdInRedis, String username)
            throws EntityNotFoundException {
        Member member = memberService.getMember(newCurrMemberIdInRedis);
        //member here should not null, as memberservice had already throw exception when null
        List<String> newPermissions =
                permissionService.getPermission(Arrays.asList(member.getPermissions())).stream()
                        .map(permission -> permission.getName()).collect(Collectors.toList());
        String JWT = JWTUtils.buildJWT(newPermissions, newCurrMemberIdInRedis, username);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SecurityConstants.TOKEN_PREFIX + " " + JWT);
        return headers;
    }
}
