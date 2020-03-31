package com.prc391.patra.filter;

import com.prc391.patra.config.security.PatraUserPrincipal;
import com.prc391.patra.config.security.SecurityConstants;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.utils.PatraStringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JWTAuthenticationFilter extends GenericFilterBean {

    private final UserRepository userRepository;
    private final UserRedisRepository userRedisRepository;
    private final Logger logger = Logger.getLogger("JWTAuthenticationFilter");

    public JWTAuthenticationFilter(UserRepository userRepository, UserRedisRepository userRedisRepository) {
        this.userRepository = userRepository;
        this.userRedisRepository = userRedisRepository;
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
            if (token != null) {
                Jws<Claims> claims = Jwts.parser().setSigningKey(SecurityConstants.SECRET)
                        .parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""));
                Claims body = claims.getBody();
                String username = body.getSubject();
                List<String> authorities = body.get(SecurityConstants.JWT_CLAIMS_AUTHORITY, List.class);
                if (CollectionUtils.isEmpty(authorities)) {
                    authorities = new ArrayList<>();
                }
                String currMemberIdInToken = body.get(SecurityConstants.JWT_CLAIMS_CURR_MEMBER_ID, String.class);

                //Get user in redis and check its' current member id, because when currMemId in db is changed,
                //currMemId in redis would change too
                Optional<UserRedis> currentUserInRedisOpt = userRedisRepository.findById(username);
                String currMemberIdInRedis;
                List<String> memberIdsInRedis = new ArrayList<>();
                if (currentUserInRedisOpt.isPresent()) {
                    logger.log(Level.INFO, "User " + username + " in redis is exist.");
                    UserRedis userRedis = currentUserInRedisOpt.get();
                    currMemberIdInRedis = userRedis.getCurrMemberId();
                    memberIdsInRedis = userRedis.getMemberIds();
                } else {
                    //hope this condition won't happen
                    logger.log(Level.INFO, "User in redis is not exist.");
                    Optional<User> optionalCurrUser = userRepository.findById(username);
                    if (!optionalCurrUser.isPresent()) {
                        logger.log(Level.SEVERE, "User " + username + " does not exist in db!");
                        return null;
//                        throw new EntityNotFoundException("User " + username + " does not exist in db!");
                    }
                    currMemberIdInRedis = optionalCurrUser.get().getCurrMemberId();
                }

                if (!PatraStringUtils.isBlankAndEmpty(currMemberIdInToken)
                        && !PatraStringUtils.isBlankAndEmpty(currMemberIdInRedis)) {
                    if (!currMemberIdInToken.equalsIgnoreCase(currMemberIdInRedis)) {
                        logger.log(Level.INFO, "Current Member's id in redis is updated");
                        //TODO: revoke token and create new token with new currMemberId
                        currMemberIdInToken = currMemberIdInRedis;
                    }
                }

                PatraUserPrincipal principal =
                        new PatraUserPrincipal(username, null, getGrantedAuthorities(authorities),
                                null, currMemberIdInToken, memberIdsInRedis);
                return username != null ?
                        new UsernamePasswordAuthenticationToken(principal, null, getGrantedAuthorities(authorities)) : null;
            } else {
                //TODO: throw new Token Null Exception here
            }
        } catch (ExpiredJwtException ex) {
            logger.log(Level.INFO, "Header: " + ex.getHeader() + " Claims: " + ex.getClaims() + "Token expired: " + ex.getMessage());
//            throw new ExpiredJwtException(ex.getHeader() ,ex.getClaims(), "Token expired: " + ex.getMessage());
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
