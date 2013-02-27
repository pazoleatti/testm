package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.DeclarationTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeclarationDataSearchServiceImpl implements DeclarationDataSearchService {

	@Autowired
	private DeclarationDao declarationDao;

	@Autowired
	private TAUserDao taUserDao;

	@Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Autowired
	private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

	@Autowired
	private DepartmentDao departmentDao;

	@Override
	public PaginatedSearchResult<DeclarationSearchResultItem> search(DeclarationFilter declarationFilter) {
		return declarationDao.findPage(declarationFilter, declarationFilter.getSearchOrdering(),
				declarationFilter.isAscSorting(), new PaginatedSearchParams(declarationFilter.getStartIndex(),
				declarationFilter.getCountOfRecords()));
	}
}
