package com.prc391.patra.utils;

import com.prc391.patra.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    public static String buildJWT(Collection<String> authorities, String username) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(SecurityConstants.JWT_CLAIMS_AUTHORITY, authorities);
        return Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
    }

    public static Claims getClaimsBodyFromJWT(String jwt) {
        Jws<Claims> claims = Jwts.parser().setSigningKey(SecurityConstants.SECRET)
                .parseClaimsJws(jwt.replace(SecurityConstants.TOKEN_PREFIX, ""));
        return claims.getBody();
    }
}
