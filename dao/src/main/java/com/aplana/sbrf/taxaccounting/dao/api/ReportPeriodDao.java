package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.List;

/**
 * Интерфейс DAO для работы с {@link ReportPeriod отчётными периодами} 
 * @author dsultanbekov
 */
public interface ReportPeriodDao {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws com.aplana.sbrf.taxaccounting.dao.api.exception.exception.DaoException если периода с заданным идентификатором не существует
	 */
	ReportPeriod get(int reportPeriodId);

	/** Получить список отчетных периодов по идентификаторам
	 * @param reportPeriodIds список идентификаторов
	 */
	List<ReportPeriod> get(List<Integer> reportPeriodIds);
	
    /**
     * Отчетный период по налоговому периоду и периоду в справочнике "Коды, определяющие налоговый (отчётный) период"
     * @param taxPeriodId
     * @param dictTaxPeriodId
     * @return
     */
    ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, int dictTaxPeriodId);
		
	/**
	 * Возвращает список отчётных периодов, входящий в данный налоговый период. 
	 * Список отсортирован по {@link ReportPeriod#getOrder() порядковым номерам} отчётных периодов
	 * @param taxPeriodId
	 * @return список отчётных периодов, входящий в данный налоговый период, отсортированный по порядковому номеру
	 */
	List<ReportPeriod> listByTaxPeriod(int taxPeriodId);

	/**
	 *
	 * @param reportPeriod отчётный период
	 * @return идентификатор нового отчетного периода
	 */
	int save(ReportPeriod reportPeriod);

	/**
	 * Удалить период
	 * @param reportPeriodId идентификатор периода
	 */
	void remove(int reportPeriodId);

    /**
     * Список отчетных периодов для указанного вида налога и для указанных подразделений
     * @param taxType Вид налога
     * @param departmentList Список подразделений
     * @return Список отчетных периодов
     */
    public List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList);

	/**
	 * Получить список всех отчетных периодов по заданному виду налога за период. Алгоритм: ищет все отчетные периоды,
	 * которые пересекаются с указанной датой. В случае, если период не найден возвращается ошибка
	 * @param taxType вид налога
	 * @param date дата, на которую ищется период
	 * @return  список отчетных периодов
	 */
	ReportPeriod getReportPeriodByDate(TaxType taxType, Date date);
}
