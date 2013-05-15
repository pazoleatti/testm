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
	 * Возвращает предыдущий отчетный период, если такой период не найден то null 
	 * @param reportPeriodId
	 * @return следующий отчетный период
	 */
	ReportPeriod getPrevReportPeriod(int reportPeriodId);

    /**
     * Возвращает дату начала отчетного периода
     * @param reportPeriodId
     * @return
     */
    public Calendar getStartDate(int reportPeriodId);

    /**
     * Возвращает дату конца отчетного периода
     * @param reportPeriodId
     * @return
     */
    public Calendar getEndDate(int reportPeriodId);
}
