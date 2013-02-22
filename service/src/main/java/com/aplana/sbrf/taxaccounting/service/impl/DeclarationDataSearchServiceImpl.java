package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.*;
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
	public DeclarationDataFilterAvailableValues getAvailableFilterValues(int userId, TaxType taxType) {
		DeclarationDataFilterAvailableValues result = new DeclarationDataFilterAvailableValues();

		TAUser user = taUserDao.getUser(userId);
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			List<Integer> listDepartments = new LinkedList<Integer>();
			for (Department department : departmentDao.listDepartments()) {
				listDepartments.add(department.getId());
			}
			result.setDepartmentIds(listDepartments);

			List<DeclarationType> declarationTypeList = declarationTypeDao.listAllByTaxType(taxType);
			Collections.sort(declarationTypeList, new DeclarationTypeAlphanumericComparator());
			result.setDeclarationTypes(declarationTypeList);
			return result;
		}

		if (!user.hasRole(TARole.ROLE_OPERATOR)) {
			throw new AccessDeniedException("У пользователя нет прав на поиск по декларациям");
		}

		List<Integer> ddts = new LinkedList<Integer>();
		ddts.addAll(departmentDeclarationTypeDao.getDepartmentIdsByTaxType(taxType));
		result.setDepartmentIds(ddts);
		List<DeclarationType> declarationTypeList = declarationTypeDao.listAllByTaxType(taxType);
		Collections.sort(declarationTypeList, new DeclarationTypeAlphanumericComparator());
		result.setDeclarationTypes(declarationTypeList);

		return result;
	}

	@Override
	public PaginatedSearchResult<DeclarationSearchResultItem> search(DeclarationFilter declarationFilter) {
		return declarationDao.findPage(declarationFilter, declarationFilter.getSearchOrdering(),
				declarationFilter.isAscSorting(), new PaginatedSearchParams(declarationFilter.getStartIndex(),
				declarationFilter.getCountOfRecords()));
	}
}
