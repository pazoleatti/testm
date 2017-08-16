package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException;
import com.aplana.sbrf.taxaccounting.model.util.DeclarationTypeAlphanumericComparator;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
    public PagingResult<DeclarationDataJournalItem> findDeclarationDataJournalItems(DeclarationDataFilter declarationFilter, PagingParams pagingParams) {

        List<DeclarationDataJournalItem> declarationDataList = declarationDao.findPage(declarationFilter, pagingParams);

        return new PagingResult<DeclarationDataJournalItem>(declarationDataList, pagingParams.getCount());
    }

    @Override
    public DeclarationDataFilterAvailableValues getFilterAvailableValues(TAUserInfo userInfo, TaxType taxType) {
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
        result.setDeclarationTypes(declarationTypeDao.listAllByTaxType(taxType));

        result.setDefaultDepartmentId(userInfo.getUser().getDepartmentId());
        Collections.sort(result.getDeclarationTypes(), new DeclarationTypeAlphanumericComparator());
        return result;
    }

    @Override
    public List<Long> getDeclarationIds(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering, boolean asc) {
        return declarationDao.findIdsByFilter(declarationFilter, ordering, asc);
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
