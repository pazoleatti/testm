package com.aplana.sbrf.taxaccounting.dao;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

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
	 * Получить объект текущего отчётного периода по виду налога. Поиск ведется только по обычным периодам, то есть
	 * периоды для ввода остатков исключены из поиска.
	 * @param taxType вид налога
	 * @return объект представляющий текущий отчётный период по заданному виду налога, или null, если такого периода нет (еще не открыли)
	 * @throws com.aplana.sbrf.taxaccounting.exception.DaoException если в БД несколько открытых периодов по заданному виду налога
	 */
	@Deprecated
	ReportPeriod getCurrentPeriod(TaxType taxType);
	
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
     * Последний отчетный период для указанного вида налога и подразделения
     * @param taxType Тип налога
     * @param departmentId Подразделение
     * @return
     */
	@Deprecated
    ReportPeriod getLastReportPeriod(TaxType taxType, long departmentId);
}
