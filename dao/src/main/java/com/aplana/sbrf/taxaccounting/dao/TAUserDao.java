package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.TAUser;

/**
 * DAO-интерфейс для работы с пользователями системы
 * @author dsultanbekov
 */
public interface TAUserDao {
	/**
	 * Получить информацию о пользователе по идентификатору
	 * @param userId идентификатор пользователя
	 * @return объект, представляющий пользователя
	 * @throws DaoException если пользователя с таким идентификатором не существует
	 */	
	TAUser getUser(int userId);
	
	/**
	 * Получить id пользователя по логину
	 * @param login идентификатор пользователя
	 * @return userId
	 * @throws DaoException если пользователя с таким логином не существует
	 */	
	int getUsreIdbyLogin(String login);
	
	/**
	 * Добавляет нового пользователя
	 */
	int createUser(TAUser user);
	
	/**
	 * Активирует/деактивирует существующего пользователя
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void setUserIsActive(int userId, boolean isActive);

	/**
	 * Обновляет существующего пользователя. Sql строка формируется в вызывающем сервисе, 
	 * чтобы определить на какие поля пришел запрос на обновление.
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void updateUser(TAUser user);
	
	/**
	 * Возвращает полный список пользователей, включая неактивный.
	 * @return {@link List<TAUser>}
	 */
	List<Integer> getUserIds();
}
