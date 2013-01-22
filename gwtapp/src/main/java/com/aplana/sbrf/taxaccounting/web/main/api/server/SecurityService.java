package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.model.TAUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с текущим контекстом авторизации.
 *
 * Он располагается в модуле <code>gwtapp</code>, т.к. вся авторизация у нас находится на слое представления.
 *
 * @author Vitalii Samolovskikh
 */
@Service
public class SecurityService {
	private TAUserDao userDao;

	/**
	 * TODO: добавить кэширование
	 * @return текущего авторизованного пользователя.
	 */
	public TAUser currentUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String login = auth.getName();
		return userDao.getUser(login);
	}

	@Autowired
	public void setUserDao(TAUserDao userDao) {
		this.userDao = userDao;
	}
}
