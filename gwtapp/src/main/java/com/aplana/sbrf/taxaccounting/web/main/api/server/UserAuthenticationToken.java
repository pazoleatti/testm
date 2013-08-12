package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * User: avanteev
 */
public class UserAuthenticationToken extends User {

    private TAUserInfo userInfo;

    public UserAuthenticationToken(TAUserInfo userInfo, Collection<GrantedAuthority> grantedAuthorities) {
        super(userInfo.getUser().getLogin(),"notused", grantedAuthorities);
        this.userInfo = userInfo;
    }

    public UserAuthenticationToken(TAUserInfo userInfo, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(userInfo.getUser().getLogin(), "notused", enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userInfo = userInfo;
    }

    public TAUserInfo getUserInfo() {
        return userInfo;
    }
}
