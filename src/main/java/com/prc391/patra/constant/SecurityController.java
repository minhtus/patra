package com.prc391.patra.constant;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.users.UserRedisService;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.PatraStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/logout")
//@AllArgsConstructor
public class SecurityController {

    private final JwtRedisService jwtRedisService;
    private final UserRedisService userRedisService;

    Logger logger = Logger.getLogger("SecurityController");

    public SecurityController(JwtRedisService jwtRedisService, UserRedisService userRedisService) {
        this.jwtRedisService = jwtRedisService;
        this.userRedisService = userRedisService;
    }


    @GetMapping
    public ResponseEntity logout() throws UnauthorizedException {
        PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
        if (!PatraStringUtils.isBlankAndEmpty(principal.getJwt())) {
            if (!jwtRedisService.saveToRedisBlacklist(principal.getJwt())) {
                logger.log(Level.SEVERE, "JWT " + principal.getJwt() + " exists in blacklist! Check JWTAuthenticationFilter for errors!");
            }
            if (!userRedisService.deleteUserInRedis(principal.getUsername())) {
                logger.log(Level.WARNING, "User " + principal.getUsername() + " is not exist in redis!");
            }
        } else {
            logger.log(Level.SEVERE, "JWT in principal is missing!");
        }
        return ResponseEntity.ok().build();
    }
}
