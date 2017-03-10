package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Сервис для работы с текущим контекстом авторизации.
 *
 * Он располагается в модуле <code>gwtapp</code>, т.к. вся авторизация у нас находится на слое представления.
 *
 * @author Vitalii Samolovskikh
 */
@Service("securityService")
@Transactional
public class SecurityService {

    @Autowired
    private TAUserService userService;

    /**
	 * Получает текущую информацию о клиенте
	 * @return
	 */
	public TAUserInfo currentUserInfo(){
        TAUserInfo userInfo = new TAUserInfo();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
        User authenticationToken = ((User)auth.getPrincipal());
        userInfo.setUser(userService.getUser(authenticationToken.getUsername()));
        userInfo.setIp(getRemoteAddress(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest()));

        return userInfo;
	}

    private String getRemoteAddress(HttpServletRequest request) {
        Validate.notNull(request);
        String xff = request.getHeader("X-Forwarded-For");
        return StringUtils.isEmpty(xff) ? request.getRemoteAddr() : StringUtils.substringBefore(xff, ",");
    }
}
