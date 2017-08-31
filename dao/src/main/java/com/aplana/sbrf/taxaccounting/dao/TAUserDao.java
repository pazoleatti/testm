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
public interface TAUserDao {
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
}
