package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Сервис для работы с текущим контекстом авторизации.
 *
 * Он располагается в модуле <code>gwtapp</code>, т.к. вся авторизация у нас находится на слое представления.
 *
 * @author Vitalii Samolovskikh
 */
@Service("securityService")
public class SecurityService {
	private TAUserDao userDao;

	/**
	 * TODO: добавить кэширование
	 * @return текущего авторизованного пользователя.
	 */
	public TAUser currentUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		String login = auth.getName();
		int userId = userDao.getUserIdbyLogin(login);
		return userDao.getUser(userId);
	}

	public TAUser getUserById(int userId){
		if(SecurityContextHolder.getContext().getAuthentication() == null){
			return null;
		} else {
			return userDao.getUser(userId);
		}
	}

	@Autowired
	public void setUserDao(TAUserDao userDao) {
		this.userDao = userDao;
	}

	public String getIp() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr();
	}
}
