package com.aplana.sbrf.taxaccounting.dao;

import java.util.Collection;

/**
 * User: avanteev
 */
public interface EventDao {
    /**
     * Получает список доступных событий для пользователя, фильтруя по заданным параметрам.
     * Маска кодов {@code mask} пишется по правилам sql, т.е. "1__", "1%" или "90_"
     * @param roleAlias роль пользователя
     * @param notInList список кодов, которые надо исключить из выборки
     * @param mask маска кодов, которые необходимы из доступных.
     * @return список кодов
     */
    Collection<Integer> getEventCodes(String roleAlias, Collection<Integer> notInList, String... mask);
}
