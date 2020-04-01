package com.prc391.patra.filter;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
import com.prc391.patra.utils.JWTUtils;
import com.prc391.patra.utils.PatraStringUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final Logger javaLogger = Logger.getLogger("JWTLoginFilter");

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        if (!PatraStringUtils.isEmpty(username) && !PatraStringUtils.isEmpty(email)) {
            javaLogger.log(Level.WARNING, "Login with both username and email at the same time? " +
                    "Interesting, but we did not have use case for this yet :/ ");
            return null;
        }

        String password = request.getParameter("password");
        if (PatraStringUtils.isBlankAndEmpty(password)) {
            javaLogger.log(Level.INFO, "Password is null");
//            return null;
        }
        PatraUserPrincipal principal = new PatraUserPrincipal(username, password, Collections.emptyList(), email, null, null);

        return getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(principal, password, Collections.emptyList()));
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

        String JWT = JWTUtils.buildJWT(setAuthorities, currMemberId, username);
        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + " " + JWT);
        //return the jwt in body
        //Authorization header is exposed, remove return the jwt in body
//        response.getWriter().write(SecurityConstants.HEADER_STRING + " " + SecurityConstants.TOKEN_PREFIX + " " + JWT);
//        response.getWriter().flush();
//        response.getWriter().close();
    }
}
