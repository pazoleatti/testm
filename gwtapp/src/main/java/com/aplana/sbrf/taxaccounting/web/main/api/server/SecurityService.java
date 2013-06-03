package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;

import com.aplana.sbrf.taxaccounting.service.TAUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class SecurityService {

	@Autowired
	private TAUserService userService;
	
	/**
	 * Получает текущую информацию о клиенте
	 * 
	 * @return
	 */
	public TAUserInfo getCurrentUserInfo(){
		TAUserInfo userInfo = new TAUserInfo();
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		// TODO: (sgoryachkin) Инфу о пользователе нужно получать не из ДАО, 
		// а из Authentication.credentials и Authentication.details
		// ДАО должен использоваться только при авторизации
		userInfo.setUser(userService.getUser(auth.getName()));
		userInfo.setIp(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr());
		return userInfo;
	}

	/**
	 * TODO: добавить кэширование
	 * @return текущего авторизованного пользователя.
	 * 
	 * 
	 * @deprecated
	 * Используйте {@link #getCurrentUserInfo()}
	 */
	public TAUser currentUser(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		String login = auth.getName();
		return userService.getUser(login);
	}

	/**
	 * @param userId
	 * @return
	 * 
	 * @deprecated 
	 * 
	 * Информация о пользователе, если это необходимо (скорее всего в случае чужой блокировки) 
	 * в полной мере должна возвращаться в хендлер из сервисного слоя.
	 */
	@Deprecated 
	public TAUser getUserById(int userId){
		if(SecurityContextHolder.getContext().getAuthentication() == null){
			return null;
		} else {
			return userService.getUser(userId);
		}
	}



	/**
	 * @return
	 * 
	 * @deprecated
	 * Используйте {@link #getCurrentUserInfo()}
	 */
	@Deprecated
	public String getIp() {
		return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr();
	}
}
