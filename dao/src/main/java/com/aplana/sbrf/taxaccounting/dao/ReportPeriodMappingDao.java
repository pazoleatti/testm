package com.aplana.sbrf.taxaccounting.dao;

import java.util.Date;

/**
 * DAO-Интерфейс для маппинга отчетного периода
 *
 * Используется только при миграции
 *
 * @author Alexander Ivanov
 */
public interface ReportPeriodMappingDao {

    /**
     * Получение отчетного периода по идентификатору налогового периода и коду периода
     * в справочнике "Коды, определяющие налоговый (отчётный) период"
     *
     * @param taxPeriodId Id налогового периода
     * @param dictTaxPeriodId код из справочника (21, 31, 33, 34 - 1 квартал, полугодие, 3 квартал, год)
     * @return отченый период
     */
    Integer getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId);

    /**
     * Возвращает идентификатор периода
     * @param start дата начала периода
     * @return taxPeriodId
     */
    Integer getTaxPeriodByDate(Date start);
}
