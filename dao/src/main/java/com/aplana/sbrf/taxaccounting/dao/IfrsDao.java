package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.IfrsData;
import com.aplana.sbrf.taxaccounting.model.IfrsDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;

import java.util.List;

/**
 * DAO-Интерфейс для работы с отчетностями для МСФО
 */
public interface IfrsDao {
    /**
     * Создание записи об отчетности для МСФО
     * @param reportPeriodId отчетный период
     */
    void create(Integer reportPeriodId);

    /**
     * Обновление записи об отчетности для МСФО
     */
    void update(Integer reportPeriodId, String uuid);

    /**
     * Получение записи об отчетности для МСФО
     * @param reportPeriodId отчетный период
     * @return отчетность для МСФО
     */
    IfrsData get(Integer reportPeriodId);

    /**
     * Поиск записей об отчетности для МСФО по отчетным периодам
     * @param reportPeriodIds список отчетных периодов
     * @return списко отчетностей для МСФО
     */
    PagingResult<IfrsDataSearchResultItem> findByReportPeriod(List<Integer> reportPeriodIds, PagingParams pagingParams);

}
