package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

public interface TaxPeriodService {
    /**
     * Возвращяет {@link TaxPeriod} по году, создаёт если необходимо
     *
     * @param year го отчета
     * @return налоговый период {@link TaxPeriod}
     */
    TaxPeriod fetchOrCreate(int year);
}
