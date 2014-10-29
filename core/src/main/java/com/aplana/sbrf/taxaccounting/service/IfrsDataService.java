package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.IfrsData;
import com.aplana.sbrf.taxaccounting.model.IfrsDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.log.Logger;

import java.util.List;

/**
 * Интерфейс для работы с отчетностями МСФО
 * @author lhaziev
 *
 */
public interface IfrsDataService {
    /**
     * Создание записи об отчетности для МСФО
     * @param reportPeriodId отчетный период
     */
    void create(Integer reportPeriodId);

    /**
     * Проверка наличие экземпляров НФ/декларации и их статусы
     * @param logger
     * @param reportPeriodId
     * @return
     */
    boolean check(Logger logger, Integer reportPeriodId);

    /**
     * Формирование отчетности для МСФО
     * @param reportPeriodId
     */
    void calculate(Logger logger, Integer reportPeriodId);

    /**
     * Обновление записи об отчетности для МСФО
     * @param data отчетность для МСФО
     */
    void update(IfrsData data);

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

    String generateTaskKey(Integer reportPeriod);

}
