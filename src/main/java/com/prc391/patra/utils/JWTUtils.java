package com.prc391.patra.utils;

import com.prc391.patra.config.security.SecurityConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    public static String buildJWT(Collection<String> authorities, String currMemberId, String username) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(SecurityConstants.JWT_CLAIMS_AUTHORITY, authorities);
        claimsMap.put(SecurityConstants.JWT_CLAIMS_CURR_MEMBER_ID, currMemberId);
        String JWT = Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        return JWT;
    }
}
