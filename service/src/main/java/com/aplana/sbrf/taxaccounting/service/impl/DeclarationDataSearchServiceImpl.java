package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.DeclarationTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.querydsl.core.types.Order;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DeclarationDataSearchServiceImpl implements DeclarationDataSearchService {

    @Autowired
    private DeclarationDataDao declarationDao;

    @Autowired
    private DeclarationTypeDao declarationTypeDao;


    @Autowired
    private DepartmentService departmentService;

    @Override
    public Long getRowNumByFilter(DeclarationDataFilter declarationFilter) {
        return declarationDao.getRowNumByFilter(declarationFilter, declarationFilter.getSearchOrdering(),
                declarationFilter.isAscSorting(), declarationFilter.getDeclarationDataId());
    }

    @Override
    public PagingResult<DeclarationDataSearchResultItem> search(DeclarationDataFilter declarationFilter) {
        return declarationDao.findPage(declarationFilter, declarationFilter.getSearchOrdering(),
                declarationFilter.isAscSorting(), new PagingParams(declarationFilter.getStartIndex(),
                        declarationFilter.getCountOfRecords()));
    }

    @Override
    public DeclarationDataFilterAvailableValues getFilterAvailableValues(TAUserInfo userInfo, TaxType taxType, boolean isReport) {
        DeclarationDataFilterAvailableValues result = new DeclarationDataFilterAvailableValues();
        if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_UNP, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_OPER,
                TARole.F_ROLE_CONTROL_UNP, TARole.F_ROLE_CONTROL_NS, TARole.F_ROLE_OPER)) {
            // http://conf.aplana.com/pages/viewpage.action?pageId=11380670
            result.setDepartmentIds(new HashSet<Integer>(departmentService.getTaxFormDepartments(userInfo.getUser(),
                    taxType, null, null)));
        } else {
            throw new AccessDeniedException("Недостаточно прав для поиска налоговых форм");
        }
        // Все типы деклараций, соответствующие виду налога
        List<DeclarationType> declarationTypes = new ArrayList<DeclarationType>();
		for(DeclarationType declarationType: declarationTypeDao.listAllByTaxType(taxType)) {
            if (isReport && (declarationType.getId() == DeclarationType.NDFL_2_1 ||
                    declarationType.getId() == DeclarationType.NDFL_6 ||
                    declarationType.getId() == DeclarationType.NDFL_2_2)
                    && userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)) {
                declarationTypes.add(declarationType);
            } else if (!isReport) {
                if (userInfo.getUser().hasRoles(taxType, TARole.N_ROLE_CONTROL_NS, TARole.N_ROLE_CONTROL_UNP)
                        && (declarationType.getId() == DeclarationType.NDFL_PRIMARY ||
                        declarationType.getId() == DeclarationType.NDFL_CONSOLIDATE)) {
                    declarationTypes.add(declarationType);
                } else if (userInfo.getUser().hasRole(taxType, TARole.N_ROLE_OPER)
                        && (declarationType.getId() == DeclarationType.NDFL_PRIMARY)) {
                    declarationTypes.add(declarationType);
                }
            }
        }
        result.setDeclarationTypes(declarationTypes);

        result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
        Collections.sort(result.getDeclarationTypes(), new DeclarationTypeAlphanumericComparator());
        return result;
    }

    @Override
    public List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        if (asc){
            return declarationDao.findIdsByFilter(declarationFilter, ordering, Order.ASC);
        }else {
            return declarationDao.findIdsByFilter(declarationFilter, ordering, Order.DESC);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DeclarationData> getDeclarationData(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        List<Long> ids = declarationDao.findIdsByFilter(declarationFilter, ordering, asc);
        Collection<DeclarationData> datas = CollectionUtils.collect(ids, new Transformer() {
            @Override
            public Object transform(Object input) {
                return declarationDao.get((Long) input);
            }
        });
        return (List<DeclarationData>) datas;
    }

    @Override
    public List<DeclarationData> getIfrs(Integer reportPeriodId) {
        return declarationDao.getIfrs(reportPeriodId);
    }
}
