package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TAUser;

/**
 * Сервис для работы с таблицей сохраненных пользователей.
 * @author avanteev
 *
 */
public interface TAUserService {
	
	TAUser getUser(String login);
	TAUser getUser(int userId);
	void setUserIsActive(String login, boolean isActive);
	void updateUser(TAUser user);
	int createUser(TAUser user);
	List<TAUser> listAllUsers();

}
