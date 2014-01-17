package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.DeclarationTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.SourceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.Arrays.asList;

@Service
public class DeclarationDataSearchServiceImpl implements DeclarationDataSearchService {

	@Autowired
	private DeclarationDataDao declarationDao;

	@Autowired
	private DeclarationTypeDao declarationTypeDao;

	@Autowired
	private DepartmentDeclarationTypeDao departmentDeclarationTypeDao;

	@Autowired
	private SourceService sourceService;

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
		if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_UNP)) {
			// Контролёр УНП видит все виды деклараций
			result.setDeclarationTypes(declarationTypeDao.listAllByTaxType(taxType));
			// во всех подразделениях, где они есть
			result.setDepartmentIds(departmentDeclarationTypeDao.getDepartmentIdsByTaxType(taxType));
		} else if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS) || userInfo.getUser().hasRole(TARole.ROLE_CONTROL)) {
            int userDepartmentId = userInfo.getUser().getDepartmentId();

            if (userInfo.getUser().hasRole(TARole.ROLE_CONTROL_NS)) {
                List<Department> departmentList = departmentService.getTBDepartments(userInfo.getUser());
                if (departmentList.size() != 1) {
                    throw new AccessDeniedException("Ошибка назначения подразделения для роли «Контролёр НС»");
                }
                userDepartmentId = departmentList.get(0).getId();
            }

            // Контролёр видит виды деклараций, привязанные к его подразделению (для НС ближайший родительский ТБ)
            // TODO Переписать после появления новой постановки на форму в 0.3.5 http://conf.aplana.com/pages/viewpage.action?pageId=11383562
            List<DepartmentDeclarationType> ddts = sourceService.getDDTByDepartment(userDepartmentId, taxType);
            Map<Integer, DeclarationType> dtMap = new HashMap<Integer, DeclarationType>();
            for (DepartmentDeclarationType ddt : ddts) {
                int declarationTypeId = ddt.getDeclarationTypeId();
                if (!dtMap.containsKey(declarationTypeId)) {
                    DeclarationType declarationType = declarationTypeDao.get(declarationTypeId);
                    if (declarationType.getTaxType() == taxType) {
                        dtMap.put(declarationTypeId, declarationType);
                    }
                }
            }
            result.setDeclarationTypes(new ArrayList<DeclarationType>(dtMap.values()));

            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            result.setDepartmentIds(new HashSet<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    asList(taxType))));
        } else {
			throw new AccessDeniedException("Недостаточно прав для поиска деклараций");
		}

		result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
		Collections.sort(result.getDeclarationTypes(), new DeclarationTypeAlphanumericComparator());
		return result;
	}

    @Override
    public List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        return declarationDao.findIdsByFilter(declarationFilter, ordering, asc);
    }
}
