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
	
	@Override
	public DeclarationFilterAvailableValues getFilterAvailableValues(int userId, TaxType taxType) {
		DeclarationFilterAvailableValues result = new DeclarationFilterAvailableValues();
		TAUser user = taUserDao.getUser(userId);
		
		if (user.hasRole(TARole.ROLE_CONTROL_UNP)) {
			// Контролёр УНП видит все виды деклараций
			result.setDeclarationTypes(declarationTypeDao.listAllByTaxType(taxType));
			// во всех подразделениях, где они есть
			result.setDepartmentIds(departmentDeclarationTypeDao.getDepartmentIdsByTaxType(taxType));
		} else if (user.hasRole(TARole.ROLE_CONTROL)) {
			int userDepartmentId = user.getDepartmentId();
			// Контролёр видит виды деклараций, привязанные к его подразделению
			Department userDepartment = departmentDao.getDepartment(userDepartmentId);			
			List<DepartmentDeclarationType> ddts = userDepartment.getDepartmentDeclarationTypes();
			Map<Integer, DeclarationType> dtMap = new HashMap<Integer, DeclarationType>();
			for (DepartmentDeclarationType ddt: ddts) {
				int declarationTypeId = ddt.getDeclarationTypeId();
				if (!dtMap.containsKey(declarationTypeId)) {
					dtMap.put(declarationTypeId, declarationTypeDao.get(declarationTypeId));
				}
			}
			result.setDeclarationTypes(new ArrayList<DeclarationType>(dtMap.values()));
			// Контролёр видит декларации только по своему подразделению
			Set<Integer> departmentIds = new HashSet<Integer>(1);
			departmentIds.add(userDepartmentId);
			result.setDepartmentIds(departmentIds);
		} else {
			throw new AccessDeniedException("Недостаточно прав для поиска деклараций");
		}
		Collections.sort(result.getDeclarationTypes(), new DeclarationTypeAlphanumericComparator());
		return result;
	}
}
