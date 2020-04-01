package com.prc391.patra.utils;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.exceptions.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

public class ControllerSupportUtils {
    public static PatraUserPrincipal getPatraPrincipal() throws UnauthorizedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (ObjectUtils.isEmpty(authentication))
            throw new UnauthorizedException("Empty authentication");

        if (!(authentication.getPrincipal() instanceof PatraUserPrincipal)) {
            throw new UnauthorizedException("Principal is not PatraUser");
        }
        return (PatraUserPrincipal) authentication.getPrincipal();
    }
}
