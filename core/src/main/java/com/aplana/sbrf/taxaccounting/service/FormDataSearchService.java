package com.aplana.sbrf.taxaccounting.service;

import java.util.List;

import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataFilterAvailableValues;
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
	 * @throws AccessDeniedException если у пользователя нет роли, разрешающей поиск по налоговым формам
	 */
	PaginatedSearchResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUser currentUser, FormDataFilter formDataFilter);

	/**
	 * Получить список, включающий в себя департамент и его дочернии департаменты
	 * @param parentDepartmentId - идентификатор департамента, по которому выбираются дочернии департаменты
	 * @return список, включающий в себя департамент и его дочернии департаменты
	 * @deprecated - нужно использовать {@link #getAvailableFilterValues(int, TaxType)}
	 */
	@Deprecated
	List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId);

	/**
	 * Получить список видов налоговых форм к которым имеет доступ пользователь по заданному виду налога.
	 * @param taxType вид налога
     * @param userId идентификатор пользователя
	 * @return список налоговых форм с определенным видом налога
	 * @deprecated - нужно использовать {@link #getAvailableFilterValues(int, TaxType)}
	 */
	@Deprecated
	List<FormType> getAvailableFormTypes(int userId, TaxType taxType);
	
	/**
	 * Получить списки значений, которые должны быть доступны пользователю 
	 * в фильтрах на странице поиска налоговых форм 
	 * @param userId идентификатор пользователя
	 * @param taxType вид налога
	 * @return объект, содержащий списки значений, которые должны быть доступны пользователю в фильтрах
	 * @throws AccessDeniedException если у пользователя нет роли, разрешающей поиск по налоговым формам 
	 */
	FormDataFilterAvailableValues getAvailableFilterValues(int userId, TaxType taxType);
}
