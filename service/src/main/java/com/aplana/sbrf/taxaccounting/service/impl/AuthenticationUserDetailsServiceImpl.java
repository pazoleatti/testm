package com.aplana.sbrf.taxaccounting.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.aplana.sbrf.taxaccounting.dao.security.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.security.TARole;
import com.aplana.sbrf.taxaccounting.model.security.TAUser;

public class AuthenticationUserDetailsServiceImpl implements
		AuthenticationUserDetailsService<Authentication> {
	private final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private TAUserDao userDao;

	@Override
	public UserDetails loadUserDetails(Authentication token)
			throws UsernameNotFoundException {
		String userName = token.getName();
		TAUser user = userDao.getUser(userName);
		if (user == null) {
			String message = "User with login '" + userName
					+ "' was not found in TaxAccounting database";
			logger.error(message);
			throw new UsernameNotFoundException(message);
		}

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(
				user.getRoles().size());

		for (TARole role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getAlias()));
		}

		// TODO: у User есть дополнительные флаги: expired, enabled и т.д.
		// возможно в будущем задействуем и их
		return new User(userName, "notused", grantedAuthorities);
	}

}
