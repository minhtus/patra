package com.prc391.patra.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Quote from Authentication interface of Spring Security
 * "The <tt>AuthenticationManager</tt> implementation will often return an
 * <tt>Authentication</tt> containing richer information as the principal for use by
 * 	the application. Many of the authentication providers will create a
 * {@code UserDetails} object as the principal."
 *
 * So instead of using a map to store user Principal, we would use an proper object
 * which implements UserDetails of SSecurity.
 * Prepare for OAuth2
 *
 * Note: if use map, in @PostAuthorize we would use authentication.principal['username']
 * If we use this object, we would use authentication.principal.username or the above .principal['username']
 * is fine
 */
@Getter
public class PatraUserPrincipal extends User {

    private String currMemberId;

    public PatraUserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public PatraUserPrincipal(String username, String password, String currMemberId, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.currMemberId = currMemberId;
    }

//    /**
//     * Cannot use lombok getter, so use this instead
//     * @return
//     */
//    public String getCurrMemberId() {
//        return currMemberId;
//    }
}
