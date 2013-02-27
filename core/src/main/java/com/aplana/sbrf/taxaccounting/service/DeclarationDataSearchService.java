package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

/**
 * @author Eugene Stetsenko
 */
public interface DeclarationDataSearchService {

	/**
	 * Данный метод, вызывает FormDataDao#findPage() для выполнения запроса к базе по заданным параметрам фильтра.
	 * @param declarationFilter фильтр, по параметрам которого происходит поиск данных по декларациям
	 * @return список идентификаторов данных по декларациям, соответствующие критериям поиска.
	 */
	PaginatedSearchResult<DeclarationSearchResultItem> search(DeclarationFilter declarationFilter);
}
