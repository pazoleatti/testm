package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilter;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;

/**
 * @author Eugene Stetsenko
 * @author DSultanbekov
 */
public interface DeclarationDataSearchService {

	/**
	 * Данный метод, вызывает FormDataDao#findPage() для выполнения запроса к базе по заданным параметрам фильтра.
	 * @param declarationFilter фильтр, по параметрам которого происходит поиск данных по декларациям
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска.
	 */
	PaginatedSearchResult<DeclarationDataSearchResultItem> search(DeclarationDataFilter declarationFilter);
	
	/**
	 * Получить информацию о значениях, допустимых в фильтрах по декларациям для пользователя по виду налога
	 * @param userId идентификатор пользователя
	 * @param taxType вид налога
	 * @return объект, содержащий информацию о допустимых значениях фильтров для поиска по декларациям
	 * @throws AccessDeniedException если у пользователя нет ролей, необходимых для поиска деклараций
	 */
	DeclarationDataFilterAvailableValues getFilterAvailableValues(int userId, TaxType taxType);
}
