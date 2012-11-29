package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * Интерфейс, позволяющий пользователю получать данные из базы по запросу
 * @author srybakov
 *
 */
public interface FormDataSearchService {

	/**
	 * Данный метод, основываясь на userId и formDataFilter, формирует параметры фильтра {@link FormDataDaoFilter}, а
	 * затем сформированный FormDataDaoFilter передает в функцию {@link FormDataSearchDao.findByFilter()} для выполнения
	 * запроса к базе по заданным параметрам фильтра.
	 * @param userId идентификатор пользователя
	 * @param formDataFilter фильтр, по параметрам которого происходит поиск данных по отчетной форме
	 * @return список идентификаторов данных по отчётным формам, соответствующие критериям поиска.
	 */
	List<FormData> findDataByUserIdAndFilter(int userId, FormDataFilter formDataFilter);

	/**
	 * Получить список всех отчетных периодов
	 * @return список всех отчетных периодов
	 */
	List<Department> listDepartments();

	/**
	 * Получить список всех видов налоговых форм
	 * @return список всех налоговых форм
	 */
	List<FormType> listFormTypes();

	/**
	 * Получить список видов налоговых форм с определенным видом налога
	 * @param taxType вид налога
	 * @return список налоговых форм с определенным видом налога
	 */
	List<FormType> listFormTypesByTaxType(TaxType taxType);

	/**
	 * Получить список отчетных периодов с определенным видом налога
	 * @param taxType вид налога
	 * @return список отчетных периодов определенным видом налога
	 */
	List<ReportPeriod> listReportPeriodsByTaxType(TaxType taxType);
}
