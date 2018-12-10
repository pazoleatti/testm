package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.*;

import java.util.List;

/**
 * @author Eugene Stetsenko
 * @author DSultanbekov
 */
@Deprecated
public interface DeclarationDataSearchService {

    /**
     * Получние id для всех деклараций по фильтру.
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
	 * @deprecated {@link com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao#findAllIdsByFilter(DeclarationDataFilter)}
     */
	@Deprecated
    List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

    /**
     * Получние экземпляров для всех деклараций по фильтру.
     * @param declarationFilter
     * @param ordering
     * @param asc
     * @return
     */
	@Deprecated
    List<DeclarationData> getDeclarationData(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc);

}
