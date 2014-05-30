package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;

import java.util.List;

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
	 * @param userInfo информация о текущем пользователе
	 * @param formDataFilter фильтр, по параметрам которого происходит поиск данных по отчетной форме
	 * @return список идентификаторов данных по отчётным формам, соответствующие критериям поиска.
	 * @throws AccessDeniedException если у пользователя нет роли, разрешающей поиск по налоговым формам
	 */
	PagingResult<FormDataSearchResultItem> findDataByUserIdAndFilter(TAUserInfo userInfo, FormDataFilter formDataFilter);

    /**
     * Данный метод на formDataFilter формирует набор НФ
     * {@link FormDataDaoFilter}, а затем сформированный FormDataDaoFilter передает в функцию
     * {@link FormDataSearchDao.findByFilter()} для выполнения запроса к базе по заданным параметрам фильтра.
     * @param formDataFilter фильтр, по параметрам которого происходит поиск данных по отчетной форме
     * @return список идентификаторов данных по отчётным формам, соответствующие критериям поиска.
     */
    List<FormData> findDataByFilter(FormDataFilter formDataFilter);

    /**
     * Данный метод, основываясь на текущем пользователе и formDataFilter, формирует параметры фильтра и возвращает
     * все id НФ подходящич под фильтр.
     * {@link FormDataDaoFilter}, а затем сформированный FormDataDaoFilter передает в функцию
     * @param userInfo информация о текущем пользователе
     * @param formDataFilter фильтр, по параметрам которого происходит поиск данных по отчетной форме
     * @return список идентификаторов данных по отчётным формам, соответствующие критериям поиска.
     * @throws AccessDeniedException если у пользователя нет роли, разрешающей поиск по налоговым формам
     */
    List<Long> findDataIdsByUserAndFilter(TAUserInfo userInfo,  FormDataFilter formDataFilter);

	/**
	 * Получить список, включающий в себя департамент и его дочернии департаменты
	 * @param parentDepartmentId - идентификатор департамента, по которому выбираются дочернии департаменты
	 * @return список, включающий в себя департамент и его дочернии департаменты
	 * @deprecated - нужно использовать {@link #getAvailableFilterValues(TAUserInfo, TaxType)}
	 */
	@Deprecated
	List<Department> listAllDepartmentsByParentDepartmentId(int parentDepartmentId);
	
	/**
	 * Получить списки значений, которые должны быть доступны пользователю 
	 * в фильтрах на странице поиска налоговых форм 
	 * @param userInfo информация о пользователе
	 * @param taxType вид налога
	 * @return объект, содержащий списки значений, которые должны быть доступны пользователю в фильтрах
	 * @throws AccessDeniedException если у пользователя нет роли, разрешающей поиск по налоговым формам 
	 */
	FormDataFilterAvailableValues getAvailableFilterValues(TAUserInfo userInfo, TaxType taxType);

	List<FormType> getActiveFormTypeInReportPeriod(int departmentId, int reportPeriodId, TaxType taxType, TAUserInfo userInfo);

    List<FormType> getActiveFormTypeInReportPeriod(int departmentId, int reportPeriodId, TaxType taxType, TAUserInfo userInfo, List<FormDataKind> kinds);
}
