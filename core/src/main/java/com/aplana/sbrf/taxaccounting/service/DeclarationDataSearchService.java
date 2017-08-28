package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * @author Eugene Stetsenko
 * @author DSultanbekov
 */
public interface DeclarationDataSearchService {

    Long getRowNumByFilter(DeclarationDataFilter declarationFilter);

	/**
	 * Данный метод, вызывает FormDataDao#findPage() для выполнения запроса к базе по заданным параметрам фильтра.
	 * @param declarationFilter фильтр, по параметрам которого происходит поиск данных по декларациям
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска.
	 */
	PagingResult<DeclarationDataSearchResultItem> search(DeclarationDataFilter declarationFilter);
	
	/**
	 * Получить информацию о значениях, допустимых в фильтрах по декларациям для пользователя по виду налога
	 * @param userInfo информация о пользователе
	 * @param taxType вид налога
	 * @param isReport
	 * @return объект, содержащий информацию о допустимых значениях фильтров для поиска по декларациям
	 * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException если у пользователя нет ролей, необходимых для поиска деклараций
	 */
	DeclarationDataFilterAvailableValues getFilterAvailableValues(TAUserInfo userInfo, TaxType taxType, boolean isReport);

    /**
     * Получние id для всех деклараций по фильтру.
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
     */
    List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

    /**
     * Получние экземпляров для всех деклараций по фильтру.
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
     */
    List<DeclarationData> getDeclarationData(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

    /**
     * Поиск декларации необходимых для формирования отчетности для МСФО
     * @param reportPeriodId
     * @return
     */
    List<DeclarationData> getIfrs(Integer reportPeriodId);
}
