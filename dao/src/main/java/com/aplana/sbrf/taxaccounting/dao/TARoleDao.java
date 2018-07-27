package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.TARole;

import java.util.List;

/**
 * ДАО для работы с ролями пользователей.
 */
public interface TARoleDao {
    /**
     * Возвращает роль по id.
     *
     * @param id идентификатор роли
     * @return роль
     */
    TARole getRole(Integer id);

    /**
     * Возвращает роль по алиасу.
     *
     * @return роль
     */
    TARole getRoleByAlias(String alias);

    /**
     * Возвращает все роли, привязанные к проекту.
     *
     * @return список ролей НДФЛ
     */
    List<TARole> getAllNdflRoles();

    /**
     * Возвращает все роли из единой базы Сбербанка.
     *
     * @return список всех ролей
     */
    List<TARole> getAllSbrfRoles();

    /**
     * Возвращает список идентификаторов всех ролей.
     *
     * @return список идентификаторов ролей
     */
    List<Integer> getAllSbrfRoleIds();
}
