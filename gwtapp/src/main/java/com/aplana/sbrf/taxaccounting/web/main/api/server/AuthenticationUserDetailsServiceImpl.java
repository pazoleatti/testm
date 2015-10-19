package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collection;

public class AuthenticationUserDetailsServiceImpl implements AuthenticationUserDetailsService<Authentication> {

	private static final Log LOG = LogFactory.getLog(AuthenticationUserDetailsServiceImpl.class);

	@Autowired
	TAUserService userService;
	@Autowired
	private AuditService auditService;

	@Override
	public UserDetails loadUserDetails(Authentication token) {
		String userName = token.getName();

		if (!userService.existsUser(userName)) {
			String message = "User with login '" + userName + "' was not found in TaxAccounting database";
			LOG.error(message);
			throw new UsernameNotFoundException(message);
		}

		LOG.info("Получение информации пользователя по логину \"" + userName + "\" getUser()");
		TAUser user = userService.getUser(userName.toLowerCase());

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Пользователь не активен!");
        }

		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>(user.getRoles().size());
		for (TARole role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getAlias()));
		}

		TAUserInfo info = new TAUserInfo();
		info.setUser(user);
		info.setIp(
				((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRemoteAddr()
		);
		auditService.add(FormDataEvent.LOGIN, info, info.getUser().getDepartmentId(), null, null, null, null, null, null, null);

		return new UserAuthenticationToken(info, grantedAuthorities);
	}
}