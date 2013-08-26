package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TAUserInfo;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.Calendar;
import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface ReportPeriodService {

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
	boolean isActivePeriod(int reportPeriodId, long departmentId);
	
	/**
	 * Проверяем, период ли остатков
	 * 
	 * @param reportPeriodId
	 * @param departmentId
	 * @return true - если остатков иначе false (не существует тоже false)
	 * 
	 */
	boolean isBalancePeriod(int reportPeriodId, long departmentId);


	/**
	 * Открываем отчетный период для департамента.
	 * Логика описана в аналитике - Ведение периодов
	 *
	 * @param year
	 * @param dictionaryTaxPeriodId
	 * @param taxType
	 */
	void open(int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user, long departmentId, List<LogEntry> logs);

	void close(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs);

	List<DepartmentReportPeriod> listByDepartmentId(long departmentId);
	
	
	/**
	 * Получает список налоговых периодов по типу налога.
	 * 
	 * @param taxType
	 * @return
	 */
	List<TaxPeriod> listByTaxType(TaxType taxType);
	
	
	/**
	 * Получает список отчетных периодов по отчетному периоду.
	 * 
	 * @param taxPeriodId
	 * @return
	 */
	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);
	
	
	/**
	 * Получает налоговый период по ID
	 * 
	 * @param taxPeriodId
	 * @return
	 */
	TaxPeriod getTaxPeriod(int taxPeriodId);
	
	
	/**
	 * Получает отчетный период по ID
	 * 
	 * @param reportPeriodId
	 * @return
	 */
	ReportPeriod getReportPeriod(int reportPeriodId);
	
	
	
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
