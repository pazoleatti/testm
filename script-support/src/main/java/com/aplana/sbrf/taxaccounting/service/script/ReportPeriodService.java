package com.aplana.sbrf.taxaccounting.service.script;

import java.util.Calendar;
import java.util.List;


import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface ReportPeriodService {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws DAOException если периода с заданным идентификатором не существует 
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
     * Возвращает дату конца отчетного периода
     * @param reportPeriodId
     * @return
     */
    Calendar getEndDate(int reportPeriodId);

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
}
