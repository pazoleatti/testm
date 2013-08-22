package com.aplana.sbrf.taxaccounting.migration.service;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;

public class AuthenticationUserDetailsServiceImpl implements
		AuthenticationUserDetailsService<Authentication> {
	private final Log logger = LogFactory.getLog(getClass());

	@Autowired
	TAUserService userService;

	@Override
	public UserDetails loadUserDetails(Authentication token)
			throws UsernameNotFoundException {
		String userName = token.getName();
		TAUser user = userService.getUser(userName);
		if (user == null) {
			String message = "User with login '" + userName
					+ "' was not found in TaxAccounting database";
			logger.error(message);
			throw new UsernameNotFoundException(message);
		}
        logger.debug(user.getLogin() + " just login.");
        if (!user.hasRole(TARole.ROLE_ADMIN)){
            String message = "User with login '" + userName
                    + "' does not have administrator role.";
            logger.error(message);
            throw new BadCredentialsException(message);
        }

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(
				user.getRoles().size());

		for (TARole role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getAlias()));
		}

		return new User(userName, "notused", grantedAuthorities);
	}

}
