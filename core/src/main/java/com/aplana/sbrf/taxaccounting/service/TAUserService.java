package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Сервис для работы с таблицей сохраненных пользователей.
 *
 * @author avanteev
 */
public interface TAUserService {
	/**
	 * Проверяет, есть ли пользователь с таким логином.
	 *
	 * @param login проверяемый логин пользователя
	 * @return true если пользователь с таким логином есть, false если нет
	 */
	boolean existsUser(String login);

	/**
	 * Ищет пользователя по его логину
	 *
	 * @param login
	 * @return
	 */
	TAUser getUser(String login);

	/**
	 * Ищет пользователя по его коду
	 *
	 * @param userId
	 * @return
	 */
	TAUser getUser(int userId);

	/**
	 * Активирует\блокирует учетную запись пользователя
	 *
	 * @param login
	 * @param isActive
	 */
	void setUserIsActive(String login, boolean isActive);

	/**
	 * Обновляет сведения о пользователе
	 *
	 * @param user
	 */
	void updateUser(TAUser user);

	/**
	 * Заводит в системе нового пользователя
	 *
	 * @param user
	 * @return
	 */
	int createUser(TAUser user);

	/**
	 * Возвращает список заведенных в системе пользователей. Включая активных и заблокированных.
	 *
	 * @return
	 */
	List<TAUser> listAllUsers();

	/**
	 * Возвращает список только активных пользователей.
	 *
	 * @return
	 */
	List<TAUserFull> listAllFullActiveUsers();

	/**
	 * @param filter фильтер
	 * @return возвращает страницу со списком пользователей
	 */
	PagingResult<TAUserFull> getByFilter(MembersFilterData filter);

	/**
	 * Возвращает список-путь от переданного подразделения по иерархии вверх
	 * @param department
	 * @return
	 */
	List<Department> getDepartmentHierarchy(int department);

}
