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
     * @param list     записи для добавления
     * @param reportPeriodId налоговоый период
     */
    void create101(List<Income101> list, Integer reportPeriodId);

    /**
     * Создание записей в таблице INCOME_102.
     *
     * @param list     записи для добавления
     * @param reportPeriodId налоговоый период
     */
    void create102(List<Income102> list, Integer reportPeriodId);

    /**
     * Удаляет все записи в таблице INCOME_101 на выбранный период
     *
     * @param reportPeriodId налоговоый период
     */
    int delete101(Integer reportPeriodId);

    /**
     * Удаляет все записи в таблице INCOME_102 на выбранный период
     *
     * @param reportPeriodId налоговоый период
     */
    int delete102(Integer reportPeriodId);

    /**
     * Получение данных из таблицы INCOME_101
     *
     * @param reportPeriodId налоговоый период
     */
    List<Income101> getIncome101(int reportPeriodId);

    /**
     * Получение данных из таблицы INCOME_102
     *
     * @param reportPeriodId налоговоый период
     */
    List<Income102> getIncome102(int reportPeriodId);

}
