package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TARole;

import java.util.List;

/**
 * Сервис для работы с пользовательскими ролями.
 */
public interface TARoleService {
    /**
     * Получить роль по алиасу.
     *
     * @param alias алиас роли пользователя
     * @return роль
     */
    TARole getRoleByAlias(String alias);

    /**
     * Получить все роли с проекта НДФЛ.
     *
     * @return список ролей
     */
    List<TARole> getAllNdflRoles();

    /**
     * Получить все роли с проектов Сбербанка.
     *
     * @return список ролей
     */
    List<TARole> getAllSbrfRoles();
}
