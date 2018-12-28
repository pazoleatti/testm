package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * Сервис для работы с таблицей сохраненных пользователей.
 *
 * @author avanteev
 */
@ScriptExposed
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
	 * Возвращает информацию о системном пользователе
	 * @return
	 */
	TAUserInfo getSystemUserInfo();

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
     * @deprecated для вытаскиваине юзера по подразделению необходимо создать отдельный метод
     * этот создавался для представления списка на клиенте
	 */
    @Deprecated
	PagingResult<TAUserFull> getByFilter(MembersFilterData filter);

    /**
     * Получить выборку пользователей для представления "Список пользователей"
     * @param filter фильтер
     * @return возвращает страницу со списком пользователей
     */
    PagingResult<TAUserView> getUsersViewWithFilter(MembersFilterData filter);

	/**
	 * Возвращает информацию о текущем пользователе системы
	 */
	TAUser getCurrentUser();

}
