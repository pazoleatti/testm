package com.aplana.sbrf.taxaccounting.dao.api;

import com.aplana.sbrf.taxaccounting.dao.PermissionDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Интерфейс DAO для работы с {@link ReportPeriod отчётными периодами} 
 * @author dsultanbekov
 */
public interface ReportPeriodDao extends PermissionDao {
	
	/**
	 * Получить объект отчётного периода по идентификатору периода
	 * @param reportPeriodId идентификатор отчётного периода
	 * @return объект, задаваемый идентификатором
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
	 */
	ReportPeriod get(Integer reportPeriodId);

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
    ReportPeriod getByTaxPeriodAndDict(int taxPeriodId, long dictTaxPeriodId);
		
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
	Integer save(ReportPeriod reportPeriod);

	/**
	 * Удалить период
	 * @param reportPeriodId идентификатор периода
	 */
	void remove(Integer reportPeriodId);

    /**
     * Список отчетных периодов для указанного вида налога и для указанных подразделений
     * @param taxType Вид налога
     * @param departmentList Список подразделений
     * @return Список отчетных периодов
     */
    List<ReportPeriod> getPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList);

	/**
	 * Получить список всех отчетных периодов по заданному виду налога за период. Алгоритм: ищет все отчетные периоды,
	 * которые пересекаются с указанной датой. В случае, если период не найден возвращается ошибка.
	 * Если было найдено несколько отчетных периодов, то возвращает тот, у котого порядок следования минимальный
	 * @param taxType вид налога
	 * @param date дата, на которую ищется период
	 * @return  список отчетных периодов
	 */
	ReportPeriod getReportPeriodByDate(TaxType taxType, LocalDateTime date);

	/**
	 * Возвращает все периоды которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
	 * @param taxType
	 * @param startDate начало диапазона
	 * @param endDate конец диапазона
	 * @return
	 */
	List<ReportPeriod> getReportPeriodsByDate(TaxType taxType, LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Возвращает все периоды которые либо пересекаются с указанным диапазоном дат, либо полностью находятся внутри него
	 * @param taxType
	 * @param depId
	 * @param startDate начало диапазона
	 * @param endDate конец диапазона
	 * @return
	 */
	List<ReportPeriod> getReportPeriodsByDateAndDepartment(TaxType taxType, int depId, LocalDateTime startDate, LocalDateTime endDate);

	/**
	 * Список открытых периодов
	 * @param taxType тип налога
	 * @param departmentList подразделения
	 * @return список отчетных периодов
	 */
	List<ReportPeriod> getOpenPeriodsByTaxTypeAndDepartments(TaxType taxType, List<Integer> departmentList,
                                                             boolean withoutCorrect);

    /**
     * Получить корректирующие периоды
     * @param taxType тип налога
     * @param departmentId идентификатор подразделения
     * @param pagingParams
	 * @return список корректирующих периодов
     */
    PagingResult<ReportPeriod> getCorrectPeriods(TaxType taxType, int departmentId, PagingParams pagingParams);

    /**
     * Получить периоды сравнения - выборка 50
     * http://conf.aplana.com/pages/viewpage.action?pageId=20386707
     * @param taxType тип налога
     * @param departmentId идентификатор подразделения
     * @return
     */
    List<ReportPeriod> getComparativPeriods(TaxType taxType, int departmentId);

    /**
     * Отчетный период по коду и году
     */
    ReportPeriod getByTaxTypedCodeYear(TaxType taxType, String code, int year);


	/**
	 * Возвращает типы периодов по коду и дате актуальности
     *
	 * @param pagingParams*/
	PagingResult<ReportPeriodType> getPeriodType(PagingParams pagingParams);

    ReportPeriodType getReportPeriodType(Long id);

	/**
	 * Возвращает тип отчетного периода по идентификатору
	 * @param id - идентификатор
	 */
	ReportPeriodType getPeriodTypeById(Long id);
}