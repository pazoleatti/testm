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
    //TODO Слишком много параметров
	void open(int year, int dictionaryTaxPeriodId, TaxType taxType, TAUserInfo user,
	          long departmentId, List<LogEntry> logs, boolean isBalance, Date correctionDate, boolean isCorrection);

	/**
	 * Закрыть период
	 * @param taxType тип налога
	 * @param reportPeriodId идентификатор отчетного периода
	 * @param departmentId идентификатор подразделения, для которого закрывается период
	 * @param logs логер, при необходимости
	 * @param user пользователь, который выполняет действие
	 */
	void close(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs, TAUserInfo user);

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
	 *
	 * <p>Информация о периодах в конфлюенсе
	 * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     * @param reportPeriodId код отчетного периода
     * @return
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     *
     * <p>Информация о периодах в конфлюенсе
     * <a href="http://conf.aplana.com/pages/viewpage.action?pageId=9600466">Как считать отчетные периоды для разных налогов</a><p/>
     *
     * @param reportPeriodId код отчетного периода
     * @return
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     * @param reportPeriodId
     * @return
     */
	// TODO: возможно имеется в виду дата сдачи отчетности. Надо проверить (Marat Fayzullin 22.01.2014)
    Calendar getReportDate(int reportPeriodId);

    /**
     * Получает все отчетные периоды в отсортированном порядке.
     * 
     * @param taxType
     * @return
     */
    List<ReportPeriod> getAllPeriodsByTaxType(TaxType taxType, boolean backOrder);

    /**
     * Получить дату начала месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthStartDate(int reportPeriodId, int periodOrder);

    /**
     * Получить дату окончания месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthEndDate(int reportPeriodId, int periodOrder);

    /**
     * Получить отчетную дату месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthReportDate(int reportPeriodId, int periodOrder);

	/**
	 * Удалить отчетный период
	 * @param taxType тип налога
	 * @param reportPeriodId идентификатор отчетного периода
	 * @param departmentId идентификатор подразделения, для которого удаляется период
	 * @param logs логер, при необходимости
	 * @param user пользователь, который выполняет действие
	 */
	void removeReportPeriod(TaxType taxType, int reportPeriodId, long departmentId, List<LogEntry> logs, TAUserInfo user);

    /**
     * Список отчетных периодов для указанного вида налога и для указанных подразделений
     *
     * @param taxType Вид налога
     * @param departmentList Список подразделений
     * @return Список отчетных периодов
     */
    List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList);


	/**
	 * Проверяет существование периода для подразделения
	 * @param departmentId подразделение, для которого осуществляется проверка существования периода
	 * @return true - существует, false - не существует
	 */
	boolean existForDepartment(Integer departmentId, long reportPeriodId);

	/**
	 * Проверяет статус периода ОТКРЫТ, ЗАКРЫТ ИЛИ НЕСУЩЕСТВУЕТ
	 * @param taxType
	 * @param year
	 * @param balancePeriod
	 * @param departmentId
	 * @param dictionaryTaxPeriodId
	 * @return
	 */
	PeriodStatusBeforeOpen checkPeriodStatusBeforeOpen(TaxType taxType, int year, boolean balancePeriod, long departmentId, long dictionaryTaxPeriodId);
}
