package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.Income101;
import com.aplana.sbrf.taxaccounting.model.Income102;

import java.util.List;

/**
 * Дао для формы "Загрузка бухгалтерской отчётности из xls"
 *
 * @author Stanislav Yasinskiy
 */
public interface BookerStatementsDao {

    /**
     * Создание записей в таблице INCOME_101.
     *
     * @param list         записи для добавления
     * @param periodID     налоговоый период
     */
    void create101(List<Income101> list, Integer periodID);

    /**
     * Создание записей в таблице INCOME_102.
     *
     * @param list         записи для добавления
     * @param periodID     налоговоый период
     */
    void create102(List<Income102> list, Integer periodID);

    /**
     * Удаляет все записи в таблице INCOME_101 на выбранный период
     *
     * @param periodID     налоговоый период
     */
    void delete101(Integer periodID);

    /**
     * Удаляет все записи в таблице INCOME_102 на выбранный период
     *
     * @param periodID     налоговоый период
     */
    void delete102(Integer periodID);
}
