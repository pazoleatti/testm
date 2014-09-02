package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

import java.util.Date;
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
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
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
    List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList);

    /**
     * Список отчетных периодов для указанного вида налога и для указанных подразделений
     * @param taxTypes Виды налога
     * @param departmentList Список подразделений
     * @return Список отчетных периодов
     */
    List<Long> getPeriodsByTaxTypesAndDepartments(List<TaxType> taxTypes, List<Integer> departmentList);

	/**
	 * Получить список всех отчетных периодов по заданному виду налога за период. Алгоритм: ищет все отчетные периоды,
	 * которые пересекаются с указанной датой. В случае, если период не найден возвращается ошибка.
	 * Если было найдено несколько отчетных периодов, то возвращает тот, у котого порядок следования минимальный
	 * @param taxType вид налога
	 * @param date дата, на которую ищется период
	 * @return  список отчетных периодов
	 */
	ReportPeriod getReportPeriodByDate(TaxType taxType, Date date);

	/**
	 * Возвращает все периоды которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
	 * @param taxType
	 * @param startDate начало диапазона
	 * @param endDate конец диапазона
	 * @return
	 */
	List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, Date startDate, Date endDate);

	/**
	 * Список открытых периодов
	 * @param taxType тип налога
	 * @param departmentList подразделения
	 * @param withoutBalance true - без периодов ввода остатков, false - с периодами ввода остатков
	 * @return список отчетных периодов
	 */
	List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                             boolean withoutBalance, boolean withoutCorrect);

    /**
     * Получить корректирующие периоды
     * @param taxType тип налога
     * @param departmentId идентификатор подразделения
     * @return список корректирующих периодов
     */
    List<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId);

    /**
     * Проверить существование экземпляров НФ данной версии макета, отчетный период которых закрыт.
     *
     * @param formTemplateId идентификатор макета НФ
     * @return список закрытых периодов, где существует экземпляр НФ данной версии
     */
    List<ReportPeriod> getClosedPeriodsForFormTemplate(Integer formTemplateId);

    /**
     * Получить список периодов входящих между данными датами
     * @param taxType
     * @param startDate
     * @param endDate
     * @return
     */
    List<ReportPeriod> getReportPeriodsInRange(TaxType taxType, Date startDate, Date endDate);

    /**
     * Отчетный период по коду и году
     */
    ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year);
}