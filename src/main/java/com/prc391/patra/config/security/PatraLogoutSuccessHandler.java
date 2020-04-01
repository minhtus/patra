package com.prc391.patra.config.security;

import com.prc391.patra.exceptions.UnauthorizedException;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.utils.ControllerSupportUtils;
import com.prc391.patra.utils.PatraStringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatraLogoutSuccessHandler extends
        SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    Logger logger = Logger.getLogger("LogoutSuccessHandler");


    public PatraLogoutSuccessHandler() {

    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.log(Level.INFO, "Logout handler here");
//        try {
//            PatraUserPrincipal principal = ControllerSupportUtils.getPatraPrincipal();
//            if (!PatraStringUtils.isBlankAndEmpty(principal.getJwt())) {
//                if (!jwtRedisService.saveToRedisBlacklist(principal.getJwt())) {
//                    logger.log(Level.SEVERE, "JWT " + principal.getJwt() + " exists in blacklist! Check JWTAuthenticationFilter for errors!");
//                }
//            } else {
//                logger.log(Level.SEVERE, "JWT in principal is missing!");
//            }
//        } catch (UnauthorizedException e) {
//            logger.log(Level.SEVERE, "User did not login properly, no principal is found, which means no jwt is passed!");
//        }

        super.onLogoutSuccess(request, response, authentication);
    }
}
