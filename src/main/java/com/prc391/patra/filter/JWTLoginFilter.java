package com.prc391.patra.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.prc391.patra.users.TokenAuthenticationService.EXPIRATION_TIME;
import static com.prc391.patra.users.TokenAuthenticationService.HEADER_STRING;
import static com.prc391.patra.users.TokenAuthenticationService.SECRET;
import static com.prc391.patra.users.TokenAuthenticationService.TOKEN_PREFIX;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        return getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        TokenAuthenticationService.addAuthentication(response, authResult.getName());
//        String authorizationString = response.getHeader("Authorization");

        String username = authResult.getName();
        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) authResult.getAuthorities();
        Set<SimpleGrantedAuthority> setAuthorities = new HashSet<>();
        setAuthorities.addAll(authorities);
        Map<String, Object> claimsAuthorities = new HashMap<>();
//        for (SimpleGrantedAuthority authority : authorities) {
//            claimsAuthorities.put("authority",authority.getAuthority());
//        }
        claimsAuthorities.put("authorities", setAuthorities);
        String JWT = Jwts.builder()
                .setClaims(claimsAuthorities)
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
    }
}
