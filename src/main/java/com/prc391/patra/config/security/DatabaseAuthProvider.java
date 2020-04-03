package com.prc391.patra.config.security;

import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.users.permission.PermissionRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

//From JWTLoginFilter to DatabaseAuthProvider
@Component
public class DatabaseAuthProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRedisRepository userRedisRepository;
    private final ModelMapper mapper;

    @Autowired
    public DatabaseAuthProvider(UserRepository userRepository, PermissionRepository permissionRepository, MemberRepository memberRepository, UserRedisRepository userRedisRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.memberRepository = memberRepository;
        this.userRedisRepository = userRedisRepository;
        this.mapper = mapper;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Username or email
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        User user = getLogin(username);

        if (user == null) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!passwordEncoder.matches(password, user.getPassHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException("User account is disabled");
        }

        UserRedis userRedis = mapper.map(user, UserRedis.class);
        Map<String, String> orgPermission = memberRepository.getAllByUsername(user.getUsername()).stream()
                .collect(Collectors.toMap(Member::getOrgId, Member::getPermission));
        userRedis.setOrgPermissions(orgPermission);
        userRedisRepository.save(userRedis);
        PatraUserPrincipal principal = new PatraUserPrincipal(user.getUsername(), Collections.emptyList(), null);
        return new UsernamePasswordAuthenticationToken(principal, password);
    }

    private User getLogin(String usernameOrEmail) {
        if (usernameOrEmail.contains("@")) {
            return userRepository.getUserByEmail(usernameOrEmail);
        } else {
            return userRepository.findById(usernameOrEmail)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private Collection<? extends GrantedAuthority> getAuthoritiesForPermission(
            Collection<Long> permissionIds) {
        List<String> permissions = new ArrayList<>();
        for (Long id : permissionIds) {
            permissions.add(permissionRepository.findById(id).get().getName());
        }
        return getGrantedAuthorities(permissions);
    }

    /**
     * Convert permission to SimpleGrantedAuthority
     *
     * @param permissions permission can bien
     * @return SimpleGrantedAuthority cua permission
     */
    private List<GrantedAuthority> getGrantedAuthorities(List<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return authorities;
    }
}
