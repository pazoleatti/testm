package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

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
	PagingResult<DeclarationDataSearchResultItem> search(DeclarationDataFilter declarationFilter);

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

}
