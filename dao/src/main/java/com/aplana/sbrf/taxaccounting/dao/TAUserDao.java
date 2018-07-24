package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.MembersFilterData;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TAUserView;

import java.util.List;

/**
 * DAO-интерфейс для работы с пользователями системы.
 *
 * @author dsultanbekov
 */
public interface TAUserDao extends PermissionDao {

    /**
     * Получить информацию о пользователе по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return объект, представляющий пользователя
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если пользователя с таким идентификатором не существует
     */
    TAUser getUser(int userId);

    /**
     * Получить id пользователя по логину.
     *
     * @param login идентификатор пользователя
     * @return userId
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если пользователя с таким логином не существует
     */
    int getUserIdByLogin(String login);

    /**
     * Возвращает полный список id пользователей, включая заблокированных.
     *
     * @return список id всех пользователей
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если возникли проблемы на уровне запроса к БД
     */
    List<Integer> getAllUserIds();

    /**
     * Проверка, обладает ли пользователь ролью.
     *
     * @param role текстовое значение роли пользователя
     * @return обладает ли пользователь этой ролью
     */
    // TODO: Этот метод должен возвращать boolean, разобраться почему не так
    int checkUserRole(String role);

    /**
     * Выборка id пользователей, подходящих под фильтр.
     *
     * @param filter фильтр пользователей
     * @return список id пользователей
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если возникли проблемы на уровне запроса к БД
     */
    List<Integer> getUserIdsByFilter(MembersFilterData filter);

    /**
     * Число пользователей, подходящих под фильтр.
     *
     * @param filter фильтр пользователей
     * @return число пользователей
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если возникли проблемы на уровне запроса к БД
     */
    int countUsersByFilter(MembersFilterData filter);

    /**
     * Проверяет, есть ли пользователь с таким логином.
     *
     * @param login проверяемый логин пользователя
     * @return true если пользователь с таким логином есть, false если нет
     */
    boolean existsUser(String login);

    /**
     * Выборка сортированных данных для страницы "Список пользователей", подходящих под фильтр.
     *
     * @param filter данные фильтра
     * @return список данных пользователя
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если возникли проблемы на уровне запроса к БД
     */
    PagingResult<TAUserView> getUserViewByFilter(MembersFilterData filter);
}
