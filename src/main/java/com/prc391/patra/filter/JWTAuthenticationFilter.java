package com.prc391.patra.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.prc391.patra.users.TokenAuthenticationService.HEADER_STRING;
import static com.prc391.patra.users.TokenAuthenticationService.SECRET;
import static com.prc391.patra.users.TokenAuthenticationService.TOKEN_PREFIX;

public class JWTAuthenticationFilter extends GenericFilterBean {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = this.getAuthentication((HttpServletRequest) request);

//        if (authentication != null) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
//        }

        chain.doFilter(request, response);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            Jws<Claims> claims = Jwts.parser().setSigningKey(SECRET)
                    .parseClaimsJws(token.replace(TOKEN_PREFIX, ""));
            Claims body = claims.getBody();
            String user = body.getSubject();
            List<Map<String, Object>> authorities = body.get("authorities", List.class);
            return user != null ?
                    new UsernamePasswordAuthenticationToken(user, null, getGrantedAuthorities(authorities)) : null;
        }
        //TODO: throw exception
        return null;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<Map<String, Object>> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Map permission : permissions) {
            authorities.add(new SimpleGrantedAuthority((String) permission.get("authority")));
        }
        return authorities;
    }
}
