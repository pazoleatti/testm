package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Сервис для работы с отчетными периодами
 */
public interface PeriodService {

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
	void open(int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user,
	          long departmentId, List<LogEntry> logs, boolean isBalance);

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
	 * Получает последний заведенный налоговый период для типа налога
	 * 
	 * @param taxPeriodId
	 * @return
	 */
	TaxPeriod getLastTaxPeriod(TaxType taxType);
	
	
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

    /**
     * Возвращает дату конца отчетного периода
     * Дата высчитывается прибавлением смещения в месяцах к дате налогового периода
     * Смещение в месяцах вычисляется путем суммирования длительности предыдущих
     * отчетных периодов в данном налоговом периоде.
     *
     * Для отчетных периодов относящихся к налоговому периоду с типом "налог на прибыль"
     * смещение считается по другому алгоритму
     *
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId
     * @return
     */
    public Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     * @param reportPeriodId
     * @return
     */
    public Calendar getReportDate(int reportPeriodId);

    /**
     * Получает все отчетные периоды в отсортированном порядке.
     * 
     * @param taxType
     * @return
     */
    public List<ReportPeriod> getAllPeriodsByTaxType(TaxType taxType, boolean backOrder);

    /**
     * Получить дату начала месяца.
     *
     * @param formData данные формы
     * @return
     */
    public Calendar getMonthStartDate(FormData formData);

    /**
     * Получить дату окончания месяца.
     *
     * @param formData данные формы
     * @return
     */
    public Calendar getMonthEndDate(FormData formData);

    /**
     * Получить отчетную дату месяцы месяца.
     *
     * @param formData данные формы
     * @return
     */
    public Calendar getMonthReportDate(FormData formData);
}
