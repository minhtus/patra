package com.prc391.patra.filter;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.utils.JWTUtils;
import com.prc391.patra.utils.PatraStringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTAuthenticationFilter extends OncePerRequestFilter {


    private final JwtRedisService jwtRedisService;
    private final Logger logger = Logger.getLogger("JWTAuthenticationFilter");

    public JWTAuthenticationFilter(JwtRedisService jwtRedisService) {
        this.jwtRedisService = jwtRedisService;
    }


    @SuppressWarnings("unchecked")
    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        try {
            if (token != null) {
                if (!jwtRedisService.isExistInBlacklist(token)) {
                    Claims body = JWTUtils.getClaimsBodyFromJWT(token);
                    List<String> authorities = body.get(SecurityConstants.JWT_CLAIMS_AUTHORITY, List.class);
                    if (CollectionUtils.isEmpty(authorities)) {
                        authorities = new ArrayList<>();
                    }
                    String username = body.getSubject();
                    PatraUserPrincipal principal = new PatraUserPrincipal(username, getGrantedAuthorities(authorities), token);
                    return !PatraStringUtils.isBlankAndEmpty(username) ?
                            new UsernamePasswordAuthenticationToken(principal, null, getGrantedAuthorities(authorities)) : null;
                } else {
                     logger.log(Level.INFO, "JWT " + token + " exists in blacklist");
                }
            }
        } catch (ExpiredJwtException ex) {
            logger.log(Level.INFO, "Header: " + ex.getHeader() + " Claims: " + ex.getClaims() + "Token expired: " + ex.getMessage());
        } catch (SignatureException ex) {
            logger.info("Invalid signed jwt");
        } catch (MalformedJwtException ex) {
            logger.info("Malformed jwt");
        }
        //TODO: throw exception
        return null;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        Authentication authentication = this.getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

//    private List<GrantedAuthority> getGrantedAuthorities(List<Map<String, Object>> permissions) {
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        for (Map permission : permissions) {
//            authorities.add(new SimpleGrantedAuthority((String) permission.get("authority")));
//        }
//        return authorities;
//    }
}
