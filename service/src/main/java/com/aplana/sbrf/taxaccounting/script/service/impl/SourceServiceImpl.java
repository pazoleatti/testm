package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.Relation;
import com.aplana.sbrf.taxaccounting.script.service.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service("sourceService")
public class SourceServiceImpl implements SourceService {

    @Autowired
    private SourceDao sourceDao;

    @Override
    public void addDeclarationConsolidationInfo(Long tgtDeclarationId, Collection<Long> srcFormDataIds) {
        sourceDao.addDeclarationConsolidationInfo(tgtDeclarationId, srcFormDataIds);
    }

    @Override
    public void deleteDeclarationConsolidateInfo(long targetDeclarationDataId) {
        sourceDao.deleteDeclarationConsolidateInfo(targetDeclarationDataId);
    }

    @Override
    public List<Relation> getDeclarationDestinationsInfo(DeclarationData declarationData) {
        return sourceDao.getDestinationsInfo(declarationData.getId());
    }
}
