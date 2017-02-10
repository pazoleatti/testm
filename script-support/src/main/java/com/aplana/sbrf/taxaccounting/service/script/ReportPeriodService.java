package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@ScriptExposed
public interface ReportPeriodService {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 */
	ReportPeriod get(int reportPeriodId);
	
	/**
	 * Возвращает список отчётных периодов, входящий в данный налоговый период. 
	 * Список отсортирован по {@link ReportPeriod#getOrder() порядковым номерам} отчётных периодов
	 * @param taxPeriodId
	 * @return список отчётных периодов, входящий в данный налоговый период, отсортированный по порядковому номеру
	 */
    @SuppressWarnings("unused")
	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);
	
	/**
	 * Возвращает предыдущий отчетный период, не привязываясь к налоговому периоду,
     * т.е. если запрашивают предыдущий отчетный период первого отчетного периода в налоговом,
     * то функция возвращает последний отчетный период предыдущего налогового периода,
     * если такой период не найден то null
	 */
	ReportPeriod getPrevReportPeriod(int reportPeriodId);

    /**
     * Возвращает дату начала отчетного периода
     */
    Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает календарную дату начала отчетного периода
     */
    Calendar getCalendarStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     */
    Calendar getEndDate(int reportPeriodId);

    /**
     * Возвращает "отчетную дату" если требуется в чтз
     * Отчетная дата = дата конца периода + 1 день
     */
    @SuppressWarnings("unused")
    Calendar getReportDate(int reportPeriodId);

    /**
     * Получить дату начала месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     */
    @SuppressWarnings("unused")
    Calendar getMonthStartDate(int reportPeriodId, int periodOrder);

    /**
     * Получить дату окончания месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     */
    @SuppressWarnings("unused")
    Calendar getMonthEndDate(int reportPeriodId, int periodOrder);

    /**
     * Получить отчетную дату месяцы месяца.
     *
     * @param reportPeriodId идентификатор отчетного период
     * @param periodOrder очередность месяца в периоде (значение из formData.periodOrder)
     * @return
     */
    @SuppressWarnings("unused")
    Calendar getMonthReportDate(int reportPeriodId, int periodOrder);

    /**
     * Возвращает все периоды по виду налога, которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
     * @param taxType Вид налога
     * @param startDate Начало периода
     * @param endDate Конец периода
     */
    @SuppressWarnings("unused")
    List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate);

    /**
     * Номер корректирующего периода
     */
    @SuppressWarnings("unused")
    Integer getCorrectionNumber(int departmentReportPeriodId);

    /**
     * Отчетный период по коду и году
     */
    ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year);

}
