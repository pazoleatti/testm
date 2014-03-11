package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.DeclarationTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;

@Service
public class DeclarationDataSearchServiceImpl implements DeclarationDataSearchService {

	@Autowired
	private DeclarationDataDao declarationDao;

	@Autowired
	private DeclarationTypeDao declarationTypeDao;


    @Autowired
    private DepartmentService departmentService;

	@Override
	public PagingResult<DeclarationDataSearchResultItem> search(DeclarationDataFilter declarationFilter) {
		return declarationDao.findPage(declarationFilter, declarationFilter.getSearchOrdering(),
				declarationFilter.isAscSorting(), new PagingParams(declarationFilter.getStartIndex(),
				declarationFilter.getCountOfRecords()));
	}
	
	@Override
	public DeclarationDataFilterAvailableValues getFilterAvailableValues(TAUserInfo userInfo, TaxType taxType) {
		DeclarationDataFilterAvailableValues result = new DeclarationDataFilterAvailableValues();
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
            if (!userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP) && userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                List<Department> departmentList = departmentService.getTBDepartments(userInfo.getUser());
                if (departmentList.size() != 1) {
                    throw new AccessDeniedException("Ошибка назначения подразделения для роли «Контролёр НС»");
                }
            }
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            result.setDepartmentIds(new HashSet<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    asList(taxType))));
        } else {
			throw new AccessDeniedException("Недостаточно прав для поиска деклараций");
		}
        // Все типы деклараций, соответствующие виду налога
        result.setDeclarationTypes(declarationTypeDao.listAllByTaxType(taxType));

		result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
		Collections.sort(result.getDeclarationTypes(), new DeclarationTypeAlphanumericComparator());
		return result;
	}

    @Override
    public List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        return declarationDao.findIdsByFilter(declarationFilter, ordering, asc);
    }
}
