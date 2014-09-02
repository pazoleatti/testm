package com.aplana.sbrf.taxaccounting.service.script;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface ReportPeriodService {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
	 */
	ReportPeriod get(int reportPeriodId);
	
	/**
	 * Возвращает список отчётных периодов, входящий в данный налоговый период. 
	 * Список отсортирован по {@link ReportPeriod#getOrder() порядковым номерам} отчётных периодов
	 * @param taxPeriodId
	 * @return список отчётных периодов, входящий в данный налоговый период, отсортированный по порядковому номеру
	 */
	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);
	
	/**
	 * Возвращает предыдущий отчетный период, не привязываясь к налоговому периоду,
     * т.е. если запрашивают предыдущий отчетный период первого отчетного периода в налоговом,
     * то функция возвращает последний отчетный период предыдущего налогового периода,
     * если такой период не найден то null
     *
	 * @param reportPeriodId
	 * @return предыдущий отчетный период
	 */
	ReportPeriod getPrevReportPeriod(int reportPeriodId);

    /**
     * Возвращает дату начала отчетного периода
     * @param reportPeriodId
     * @return
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает календарную дату начала отчетного периода
     * @param reportPeriodId
     * @return
     */
    Calendar getCalendarStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     * @param reportPeriodId
     * @return
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     * @param reportPeriodId
     * @return
     */
    Calendar getReportDate(int reportPeriodId);

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
     * Получить отчетную дату месяцы месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    Calendar getMonthReportDate(int reportPeriodId, int periodOrder);

    /**
     * Возвращает все периоды по виду налога, которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     * @param taxType Вид налога
     * @param startDate Начало периода
     * @param endDate Конец периода
     * @return Список отчетных периодов
     */
    List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate);

    /**
     * Возвращает все периоды по виду налога, которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     * @param taxType Вид налога
     * @param startDate Начало периода
     * @param endDate Конец периода
     * @return Список отчетных периодов
     */
    List<ReportPeriod> getReportPeriodsInRange(TaxType taxType, Date startDate, Date endDate);

    /**
     * Получить номер корректирующего периода.
     * @param reportPeriodId идентификатор отчетного период
     * @param departmentId идентификатор подразделения
     */
    int getCorrectionPeriodNumber(int reportPeriodId, long departmentId);
}
