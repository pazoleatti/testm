package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriodType;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

public interface ReportPeriodService {
    /**
     * Возвращяет отчетный период {@link ReportPeriod}, создаёт новый при необходимости
     *
     * @param taxPeriod        налоговый период
     * @param reportPeriodType данные по отчетному периоду
     * @param formTypeId       ссылка на вид отчетности
     * @return отчетный период {@link ReportPeriod}
     */
    ReportPeriod fetchOrCreate(TaxPeriod taxPeriod, ReportPeriodType reportPeriodType, Integer formTypeId);
}
