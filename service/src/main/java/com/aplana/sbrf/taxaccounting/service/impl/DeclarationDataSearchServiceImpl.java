package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataSearchService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class DeclarationDataSearchServiceImpl implements DeclarationDataSearchService {

    private DeclarationDataDao declarationDao;

    public DeclarationDataSearchServiceImpl(DeclarationDataDao declarationDataDao) {
        declarationDao = declarationDataDao;
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
}