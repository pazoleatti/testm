package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;

/**
 * Интерфейс DAO для работы с {@link ReportPeriod отчётными периодами} 
 * @author dsultanbekov
 */
public interface ReportPeriodDao {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если периода с заданным идентификатором не существует
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
	 *
	 * @param reportPeriodId
	 * @param active
	 */
	@Deprecated
	void changeActive(int reportPeriodId, boolean active);

	/**
	 *
	 * @param reportPeriod отчётный период
	 * @return идентификатор нового отчетного периода
	 */
	//TODO: Перименовать в save
	int add(ReportPeriod reportPeriod);


    /**
     * Отчетный период по налоговому периоду и периоду в справочнике "Коды, определяющие налоговый (отчётный) период"
     * @param taxPeriodId
     * @param dictTaxPeriodId
     * @return
     */
    ReportPeriod getReportPeriodByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId);
}
