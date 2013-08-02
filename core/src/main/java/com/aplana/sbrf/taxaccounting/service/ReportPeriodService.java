package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface ReportPeriodService {

	ReportPeriod get(int reportPeriodId);

	ReportPeriod getCurrentPeriod(TaxType taxType);

	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);

	List<ReportPeriod> listByTaxPeriodAndDepartment(int taxPeriodId, long departmentId);

	void closePeriod(int reportPeriodId);

	void openPeriod(int reportPeriodId);

	int add(ReportPeriod reportPeriod);

    /**
     * Последний отчетный период для указанного вида налога и подразделения
     * @param taxType Тип налога
     * @param departmentId Подразделение
     * @return
     */
    ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId);
}
