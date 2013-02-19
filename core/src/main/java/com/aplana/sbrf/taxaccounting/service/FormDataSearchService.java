package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TAUser;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * Интерфейс, позволяющий пользователю получать данные из базы по запросу
 * @author srybakov
 *
 */
public interface FormDataSearchService {
	/**
	 * Данный метод, основываясь на текущем пользователе и formDataFilter, формирует параметры фильтра
	 * {@link FormDataDaoFilter}, а затем сформированный FormDataDaoFilter передает в функцию
	 * {@link FormDataSearchDao.findByFilter()} для выполнения запроса к базе по заданным параметрам фильтра.
	 * @param currentUser текущий пользователь
	 * @param formDataFilter фильтр, по параметрам которого происходит поиск данных по отчетной форме
	 * @return список идентификаторов данных по отчётным формам, соответствующие критериям поиска.
	 */
	PaginatedSearchResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUser currentUser, FormDataFilter formDataFilter);

	/**
	 * Получить список, включающий в себя департамент и его дочернии департаменты
	 * @param parentDepartmentId - идентификатор департамента, по которому выбираются дочернии департаменты
	 * @return список, включающий в себя департамент и его дочернии департаменты
	 */
	List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId);

	/**
	 * Получить список всех видов налоговых форм
	 * @return список всех налоговых форм
	 */
	List<FormType> listFormTypes();

	/**
	 * Получить список видов налоговых форм с определенным видом налога и для определенного пользователя.
     * С каждым подразделением связывается список видов налоговых форм, с которыми можно работать сотрудникам данного подразделения.
	 * @param taxType вид налога
     * @param userId идентификатор пользователя
	 * @return список налоговых форм с определенным видом налога
	 */
	List<FormType> getAvailableFormTypes(int userId, TaxType taxType);
}
