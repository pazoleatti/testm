package com.aplana.sbrf.taxaccounting.dao;

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
}
