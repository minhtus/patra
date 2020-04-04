package com.prc391.patra.users;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityExistedException;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.exceptions.InvalidInputException;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.members.MemberService;
import com.prc391.patra.members.responses.MemberResponse;
import com.prc391.patra.orgs.Organization;
import com.prc391.patra.users.requests.ChangePassRequest;
import com.prc391.patra.users.requests.CreateGoogleUserRequest;
import com.prc391.patra.users.requests.CreateUserRequest;
import com.prc391.patra.utils.JWTUtils;
import com.prc391.patra.utils.PatraStringUtils;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/v0/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;
    private final MemberService memberService;
    private final JwtRedisService jwtRedisService;
    private final GoogleLoginService googleLoginService;

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
        return ResponseEntity.ok(userService.getUser(null));
    }

    @PostMapping
    public ResponseEntity<User> registerUser(@RequestBody CreateUserRequest request) throws EntityExistedException {
        User user = mapper.map(request, User.class);
        user.setPassHash(request.getPassword());
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/google/login")
    public ResponseEntity<User> registerGoogleUser(
            @RequestBody CreateGoogleUserRequest request) throws UnauthorizedException {
        User result = googleLoginService.googleLogin(request.getGoogleIdToken());
        HttpHeaders headers = getNewAuthorizationHeader(result.getUsername());
        return ResponseEntity.ok().headers(headers).body(result);
    }

    @PostMapping("/change-pass")
    public ResponseEntity changePassword(
            @RequestBody ChangePassRequest changePassRequest,
            @ApiIgnore @AuthenticationPrincipal Object principal) throws UnauthorizedException, EntityNotFoundException, InvalidInputException {
        if (!(principal instanceof PatraUserPrincipal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        PatraUserPrincipal patraUserPrincipal = (PatraUserPrincipal) principal;

        userService.changePassword(changePassRequest);
        HttpHeaders headers = getNewAuthorizationHeader(patraUserPrincipal);
        return ResponseEntity.ok().headers(headers).build();
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

    private HttpHeaders getNewAuthorizationHeader(PatraUserPrincipal principal) {
        String newJWT = JWTUtils.buildJWT(Collections.emptyList(), principal.getUsername());
        if (!PatraStringUtils.isBlankAndEmpty(principal.getJwt())) {
            jwtRedisService.saveToRedisBlacklist(principal.getJwt());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SecurityConstants.TOKEN_PREFIX + " " + newJWT);
        return headers;
    }

    private HttpHeaders getNewAuthorizationHeader(String username) {
        String newJWT = JWTUtils.buildJWT(Collections.emptyList(), username);
//        if (!PatraStringUtils.isBlankAndEmpty(principal.getJwt())) {
//            jwtRedisService.saveToRedisBlacklist(principal.getJwt());
//        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", SecurityConstants.TOKEN_PREFIX + " " + newJWT);
        return headers;
    }


}
