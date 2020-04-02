package com.prc391.patra.filter;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
import com.prc391.patra.jwt.JwtRedisService;
import com.prc391.patra.utils.JWTUtils;
import com.prc391.patra.utils.PatraStringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTAuthenticationFilter extends GenericFilterBean {


    private final JwtRedisService jwtRedisService;
    private final Logger logger = Logger.getLogger("JWTAuthenticationFilter");

    public JWTAuthenticationFilter(JwtRedisService jwtRedisService) {
        this.jwtRedisService = jwtRedisService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = this.getAuthentication((HttpServletRequest) request);

//        if (authentication != null) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
//        }

        chain.doFilter(request, response);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        try {
            if (!PatraStringUtils.isBlankAndEmpty(token)) {
                if (!jwtRedisService.isExistInBlacklist(token)) {
                    Claims body = JWTUtils.getClaimsBodyFromJWT(token);
                    String username = body.getSubject();
                    List<String> authorities = body.get(SecurityConstants.JWT_CLAIMS_AUTHORITY, List.class);
                    if (CollectionUtils.isEmpty(authorities)) {
                        authorities = new ArrayList<>();
                    }
                    String currMemberIdInToken = body.get(SecurityConstants.JWT_CLAIMS_CURR_MEMBER_ID, String.class);
                    PatraUserPrincipal principal =
                            new PatraUserPrincipal(username, null, getGrantedAuthorities(authorities),
                                    null, currMemberIdInToken, token);
                    return username != null ?
                            new UsernamePasswordAuthenticationToken(principal, null, getGrantedAuthorities(authorities)) : null;
                } else {
                     logger.log(Level.INFO, "JWT " + token + " exists in blacklist");
                }
            } else {
                //TODO: throw new Token Null Exception here
            }
        } catch (ExpiredJwtException ex) {
            logger.log(Level.INFO, "Header: " + ex.getHeader() + " Claims: " + ex.getClaims() + "Token expired: " + ex.getMessage());
//            throw new ExpiredJwtException(ex.getHeader() ,ex.getClaims(), "Token expired: " + ex.getMessage());
        } catch (JwtException ex) {
            logger.log(Level.INFO, "JWT errors: " + ex.getMessage());
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

//    private List<GrantedAuthority> getGrantedAuthorities(List<Map<String, Object>> permissions) {
//        List<GrantedAuthority> authorities = new ArrayList<>();
//        for (Map permission : permissions) {
//            authorities.add(new SimpleGrantedAuthority((String) permission.get("authority")));
//        }
//        return authorities;
//    }
}
