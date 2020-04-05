package com.prc391.patra.security.filter;

import com.prc391.patra.security.PatraUserPrincipal;
import com.prc391.patra.constant.SecurityConstants;
import com.prc391.patra.utils.JWTUtils;
import com.prc391.patra.utils.PatraStringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.logging.Logger;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

    private final Logger javaLogger = Logger.getLogger("JWTLoginFilter");

    public JWTLoginFilter(String url, AuthenticationManager authManager) {
        super(new AntPathRequestMatcher(url));
        setAuthenticationManager(authManager);
    }

    /**
     * Login endpoint
     * Request params
     * username=[username or email input]
     * password=[password input
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (PatraStringUtils.isBlankAndEmpty(username) || PatraStringUtils.isBlankAndEmpty(password)) {
            throw new IllegalArgumentException("Credentials is empty");
        }

        PatraUserPrincipal principal = new PatraUserPrincipal(username, Collections.emptyList(), null);
        return getAuthenticationManager()
                .authenticate(new UsernamePasswordAuthenticationToken(principal, password));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        PatraUserPrincipal principal = (PatraUserPrincipal) authResult.getPrincipal();
        String username = principal.getUsername();
        String JWT = JWTUtils.buildJWT(Collections.emptyList(), username);
        response.addHeader(SecurityConstants.HEADER_STRING, SecurityConstants.TOKEN_PREFIX + " " + JWT);
    }
}
