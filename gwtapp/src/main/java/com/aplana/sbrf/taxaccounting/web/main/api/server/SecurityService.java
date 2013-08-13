package com.aplana.sbrf.taxaccounting.web.main.api.server;

import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	/*@Autowired
	private TAUserService userService;*/
	
	/**
	 * Получает текущую информацию о клиенте
	 * TODO: добавить кэширование
	 * @return
	 */
	public TAUserInfo currentUserInfo(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		// TODO: (sgoryachkin) Инфу о пользователе нужно получать не из ДАО, 
		// а из Authentication.credentials и Authentication.details
		// ДАО должен использоваться только при авторизации
		/*userInfo.setUser(userService.getUser(auth.getName()));
		userInfo.setIp(((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest().getRemoteAddr());*/
        UserAuthenticationToken authenticationToken = ((UserAuthenticationToken)auth.getPrincipal());

        return authenticationToken!=null?authenticationToken.getUserInfo():null;
	}

}
