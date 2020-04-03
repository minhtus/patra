package com.prc391.patra.utils;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.exceptions.EntityNotFoundException;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.UserRedisService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class AuthorizationUtils {
    private final UserRedisService userRedisService;
    private final MemberRepository memberRepository;

    public boolean authorizeAccess(String orgId, String access) throws EntityNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PatraUserPrincipal principal = (PatraUserPrincipal) authentication.getPrincipal();
        String username = principal.getUsername();
        Map<String, String> orgPermissions = userRedisService.getUserRedis(username).getOrgPermissions();
        //TODO try to update from db if not found permission; check for null orgPermission.get(id)
        return checkAccess(orgPermissions.get(orgId), access);
    }

    private boolean checkAccess(String permission, String requestAccess) {
        if (PatraStringUtils.isBlankAndEmpty(permission))
            return false;
        if (permission.equals(requestAccess))
            return true;
        if (SecurityConstants.ADMIN_ACCESS.equals(permission)) { //admin access all
            return true;
        }
        if (SecurityConstants.WRITE_ACCESS.equals(permission) && SecurityConstants.READ_ACCESS.equals(requestAccess))
            return true;
        return false;
    }
}
