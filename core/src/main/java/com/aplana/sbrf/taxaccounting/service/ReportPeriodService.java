package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.Calendar;
import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface ReportPeriodService {

	ReportPeriod get(int reportPeriodId);
	
	TaxPeriod getTaxPeriod(int taxPeriodId);

	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);

	void closePeriod(int reportPeriodId);

	void openPeriod(int reportPeriodId);

	int add(ReportPeriod reportPeriod);

    /**
     * Последний отчетный период для указанного вида налога и подразделения
     * @param taxType Тип налога
     * @param departmentId Подразделение
     * @return
     */
    DepartmentReportPeriod getLastReportPeriod(TaxType taxType, long departmentId);
    

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
	
	
	/**
	 * Получает список налоговых периодов по типу налога.
	 * 
	 * @param taxType
	 * @return
	 */
	List<TaxPeriod> listByTaxType(TaxType taxType);
	
	
    /**
     * Возвращает дату начала отчетного периода
     * Дата высчитывается прибавлением смещения в месяцах к дате налогового периода
     * Смещение в месяцах вычисляется путем суммирования длительности предыдущих
     * отчетных периодов в данном налоговом периоде.
     *
     * Для отчетных периодов относящихся к налоговому периоду с типом "налог на прибыль"
     * смещение считается по другому алгоритму
     * @param reportPeriodId
     * @return
     */
    public Calendar getStartDate(int reportPeriodId);
}
