package com.prc391.patra.config.security;

import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.users.permission.Permission;
import com.prc391.patra.users.permission.PermissionRepository;
import com.prc391.patra.users.role.Role;
import com.prc391.patra.users.role.RoleRepository;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class DatabaseAuthProvider implements AuthenticationProvider {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private PasswordEncoder passwordEncoder;

    @Autowired
    public DatabaseAuthProvider(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userRepository.getUserByUsername(username);

        //dung config file cho dong thong bao, nhac de khoi quen
        if (user == null) {
            throw new BadCredentialsException
                    ("(username (or password) not valid message, use config file instead of hardcoding)");
        }
        if (!passwordEncoder.matches(password, user.getPassHash())) {
            throw new BadCredentialsException
                    ("(password (or username) not valid message, use config file instead of hard coding");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException
                    ("(user is disabled message, use config file instead of hard coding");
        }

        return new UsernamePasswordAuthenticationToken(
                username,
                password,
                getAuthorities(user.getRoles())
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    /**
     * lay cac authority (cac permission) de phuc vu cho viec phan quyen trong @PreAuthorize
     * @param rolesId List id cua role ma User so huu
     * @return cac permission da duoc bien thanh kieu SimpleGrantedAuthority
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Long> rolesId) {
        List<Role> roles = new ArrayList<>();
        for (Long id : rolesId) {
            roles.add(roleRepository.findById(id).get());
        }
        return getGrantedAuthorities(getPermissions(roles));
    }

    /**
     * Convert permission thanh SimpleGrantedAuthority
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

    /**
     * Ham lap de lay cac permission trong role
     * @param roles roles can lay permission
     * @return List ten cua permission
     */
    private List<String> getPermissions(Collection<Role> roles) {
        List<String> privileges = new ArrayList<>();
        List<Permission> permissionsList = new ArrayList<>();
        List<Long> privIdList = new ArrayList<>();
        for (Role role : roles) {
            privIdList = role.getPermissions();
            for (Long id : privIdList) {
                permissionsList.add(permissionRepository.findById(id).get());
            }
        }
        for (Permission item : permissionsList) {
            privileges.add(item.getName());
        }
        return privileges;
    }
}
