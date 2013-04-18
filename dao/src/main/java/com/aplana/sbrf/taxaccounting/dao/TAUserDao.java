package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TARole;
import com.aplana.sbrf.taxaccounting.model.TAUser;

/**
 * DAO-интерфейс для работы с пользователями системы
 * @author dsultanbekov
 */
public interface TAUserDao {
	/**
	 * Получить информацию о пользователе по логину
	 * @param login логин пользователя
	 * @return объект, представляющий пользователя
	 * @throws DaoException если пользователя с таким логином не существует
	 */
	TAUser getUser(String login);
	/**
	 * Получить информацию о пользователе по идентификатору
	 * @param userId идентификатор пользователя
	 * @return объект, представляющий пользователя
	 * @throws DaoException если пользователя с таким идентификатором не существует
	 */	
	TAUser getUser(int userId);
	
	/**
	 * Получить список всех ролей в системе
	 * @return список {@link TARole ролей}
	 */
	List<TARole> listRolesAll();
	
	/**
	 * Добавляет нового пользователя
	 */
	void addUser(TAUser user);
	
	/**
	 * Обновляет существующего пользователя, без ролей
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void setUserIsActive(TAUser user);
	
	/**
	 * Обновляет роли
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void updateUserRoles(TAUser user);
}
