package com.aplana.sbrf.taxaccounting.dao;


import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы с настройками подразделений
 */
public interface DepartmentConfigDao {

    /**
     * Возвращяет пары КПП и ОКТМО из настроек подразделений по определенным подразделениям
     *
     * @param departmentIds список идентификаторов подразделений
     * @param relevanceDate дата актуальности настройки
     * @return список пар КПП и ОКТМО
     */
    List<Pair<String, String>> fetchKppOktmoPairs(List<Integer> departmentIds, Date relevanceDate);
}
