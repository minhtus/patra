package com.prc391.patra.config.security;

import com.prc391.patra.members.Member;
import com.prc391.patra.members.MemberRepository;
import com.prc391.patra.users.User;
import com.prc391.patra.users.UserRedis;
import com.prc391.patra.users.UserRedisRepository;
import com.prc391.patra.users.UserRepository;
import com.prc391.patra.users.permission.Permission;
import com.prc391.patra.users.permission.PermissionRepository;
import com.prc391.patra.users.role.Role;
import com.prc391.patra.users.role.RoleRepository;
import com.prc391.patra.utils.PatraStringUtils;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//From JWTLoginFilter to DatabaseAuthProvider
@Component
public class DatabaseAuthProvider implements AuthenticationProvider {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRedisRepository userRedisRepository;

    private final ModelMapper mapper;

    @Autowired
    public DatabaseAuthProvider(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, MemberRepository memberRepository, UserRedisRepository userRedisRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.memberRepository = memberRepository;
        this.userRedisRepository = userRedisRepository;
        this.mapper = mapper;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();

        String email = ((PatraUserPrincipal) authentication.getPrincipal()).getEmail();
        String password = authentication.getCredentials().toString();

        //login using username or email
        User user = null;
        if (!StringUtils.isEmpty(username)) {
            Optional<User> optionalUser = userRepository.findById(username);
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            }
        } else if (!StringUtils.isEmpty(email)) {
            user = userRepository.getUserByEmail(email);
        } else {
            throw new BadCredentialsException("Something wrong here");
        }


        //dung config file cho dong thong bao, nhac de khoi quen
        if (user == null) {
            throw new BadCredentialsException
                    ("(username or email (or password) not valid message, use config file instead of hardcoding)");
        }
        if (!passwordEncoder.matches(password, user.getPassHash())) {
            throw new BadCredentialsException
                    ("(password (or username) not valid message, use config file instead of hard coding");
        }
        if (!user.isEnabled()) {
            throw new BadCredentialsException
                    ("(user is disabled message, use config file instead of hard coding");
        }

        //Get current working Member, then get the Member's permissions

        //save user into redis
        userRedisRepository.deleteById(user.getUsername());
        UserRedis userRedis = mapper.map(user, UserRedis.class);
        //get all member ids
        List<String> memberIds = memberRepository.getAllByUsername(user.getUsername()).stream()
                .map(member -> member.getMemberId()).collect(Collectors.toList());
        userRedis.setMemberIds(memberIds);
//        List<Member> memberList = memberRepository.getAllByUsername(user.getUsername());
//        userRedis.setMemberIds(memberList.stream().map(member -> member.getMemberId()).collect(Collectors.toList()));
        userRedisRepository.save(userRedis);
        String currMemberId = user.getCurrMemberId();
        Collection<? extends GrantedAuthority> authorities = null;
        if (!PatraStringUtils.isBlankAndEmpty(currMemberId)) {
            Optional<Member> currMember = memberRepository.findById(currMemberId);
            if (currMember.isPresent()) {
                Member member = currMember.get();
                authorities = getAuthoritiesForPermission(Arrays.asList(member.getPermissions()));
            }
        }
//        authorities = new HashSet<>();
        //use username and email got from user to build Principal, because passed username/email may be null
        String loggedInUsername = user.getUsername();
        String loggedInEmail = user.getEmail();
        PatraUserPrincipal principal = new PatraUserPrincipal(loggedInUsername, password,
                CollectionUtils.isEmpty(authorities) ? new HashSet<>() : authorities, loggedInEmail
                , currMemberId == null ? "" : currMemberId, memberIds == null ? new ArrayList<>() : memberIds);

        return new UsernamePasswordAuthenticationToken(
                principal,
                username,
                CollectionUtils.isEmpty(authorities) ? new HashSet<>() : authorities
        );
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

    /**
     * lay cac authority (cac permission) de phuc vu cho viec phan quyen trong @PreAuthorize
     *
     * @param rolesId List id cua role ma User so huu
     * @return cac permission da duoc bien thanh kieu SimpleGrantedAuthority
     */
    @Deprecated
    private Collection<? extends GrantedAuthority> getAuthorities(Collection<Long> rolesId) {
        List<Role> roles = new ArrayList<>();
        for (Long id : rolesId) {
            roles.add(roleRepository.findById(id).get());
        }
        return getGrantedAuthorities(getPermissions(roles));
    }

    /**
     * Get all permission in Role (deprecated)
     *
     * @param roles roles can lay permission
     * @return List ten cua permission
     */
    @Deprecated
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
