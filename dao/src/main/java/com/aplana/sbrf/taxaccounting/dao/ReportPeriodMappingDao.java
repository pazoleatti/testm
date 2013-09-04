package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;

/**
 * DAO-Интерфейс для маппинга отчетного периода
 * @author Alexander Ivanov
 */
public interface ReportPeriodMappingDao {

    /**
     * Получение отчетного периода по идентификатору налогового периода и коду периода
     * в справочнике "Коды, определяющие налоговый (отчётный) период"
     * @param taxPeriodId Id налогового периода
     * @param dictTaxPeriodId код из справочника (21, 31, 33, 34 - 1 квартал, полугодие, 3 квартал, год)
     * @return отченый период
     */
    ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId);
}
