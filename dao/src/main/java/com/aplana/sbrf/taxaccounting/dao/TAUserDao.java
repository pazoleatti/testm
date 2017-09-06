package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserView;

import java.util.List;

/**
 * DAO-интерфейс для работы с пользователями системы
 * @author dsultanbekov
 */
public interface TAUserDao extends PermissionDao {
	/**
	 * Получить информацию о пользователе по идентификатору
	 * @param userId идентификатор пользователя
	 * @return объект, представляющий пользователя
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если пользователя с таким идентификатором не существует
	 */	
	TAUser getUser(int userId);
	
	/**
	 * Получить id пользователя по логину
	 * @param login идентификатор пользователя
	 * @return userId
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если пользователя с таким логином не существует
	 */	
	int getUserIdByLogin(String login);
	
	/**
	 * Создает нового пользователя
	 */
	int createUser(TAUser user);
	
	/**
	 * Активирует/блокирует существующего пользователя
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void setUserIsActive(int userId, int isActive);

	/**
	 * Обновляет существующего пользователя. Sql строка формируется в вызывающем сервисе, 
	 * чтобы определить на какие поля пришел запрос на обновление.
	 * @param {@link TAUser} user идентификатор пользователя
	 */
	void updateUser(TAUser user);
	
	/**
	 * Возвращает полный список пользователей, включая заблокированных.
	 * @return {@link List<TAUser>}
	 */
	List<Integer> getUserIds();
	
	/**
	 * Неоходим только для валидации
	 * @param role
	 * @return
	 */
	int checkUserRole(String role);

	List<Integer> getByFilter(MembersFilterData filter);

    /**
     * Выборка данных пользователей по фильтру и сортировкой
     * @param filter данные фильтра
     * @return список данных пользователя. Модель представления
     */
    PagingResult<TAUserView> getUsersByFilter(MembersFilterData filter);

	int count(MembersFilterData filter);

	/**
	 * Проверяет, есть ли пользователь с таким логином.
	 * @param login проверяемый логин пользователя
	 * @return true если пользователь с таким логином есть, false если нет
	 */
	boolean existsUser(String login);

	/**
	 * Обновляет запись в справочнике "Ограничение доступа по АСНУ"
	 * @param user
	 */
	void updateUserAsnu(TAUser user);
}
