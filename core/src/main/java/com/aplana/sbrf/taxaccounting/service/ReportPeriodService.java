package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface ReportPeriodService {

	ReportPeriod get(int reportPeriodId);

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
    

	/**
	 * Проверяем, открыт ли период для департамента или нет
	 * 
	 * @param reportPeriodId
	 * @param departmentId
	 * @return
	 */
	boolean checkOpened(int reportPeriodId, long departmentId);

	/**
	 *
	 * @param reportPeriod
	 * @param year
	 * @param dictionaryTaxPeriodId
	 * @param taxType
	 */
	void open(ReportPeriod reportPeriod, int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user, long departmentId);

	List<DepartmentReportPeriod> listByDepartmentId(long departmentId);
}
