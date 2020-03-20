package com.prc391.patra.filter;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
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
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        PatraUserPrincipal principal = (PatraUserPrincipal) authResult.getPrincipal();
        String username = principal.getUsername();
        String currMemberId = principal.getCurrMemberId();
        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) authResult.getAuthorities();
        Set<String> setAuthorities = new HashSet<>();
        for (SimpleGrantedAuthority authority : authorities) {
            setAuthorities.add(authority.getAuthority());
        }
        Map<String, Object> claimsMap = new HashMap<>();
//        for (SimpleGrantedAuthority authority : authorities) {
//            claimsAuthorities.put("authority",authority.getAuthority());
//        }

        claimsMap.put(SecurityConstants.JWT_CLAIMS_AUTHORITY, setAuthorities);
        claimsMap.put(SecurityConstants.JWT_CLAIMS_CURR_MEMBER_ID, currMemberId);
        String JWT = Jwts.builder()
                .setClaims(claimsMap)
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET).compact();
        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + " " + JWT);
    }
}
