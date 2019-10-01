package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.NdflReferenceDao;
import com.aplana.sbrf.taxaccounting.model.refbook.NumFor2Ndfl;
import com.aplana.sbrf.taxaccounting.script.service.NdflReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ndflReferenceService")
public class NdflReferenceServiceImpl implements NdflReferenceService {

    @Autowired
    private NdflReferenceDao ndflReferenceDao;

    @Override
    public List<NumFor2Ndfl> getCorrSprNum(Long personId, int year, String kpp, String oktmo, int declarationTypeId) {
        return ndflReferenceDao.getCorrSprNum(personId, year, kpp, oktmo, declarationTypeId);
    }

    public Integer getNextSprNum(Integer year) {
        return ndflReferenceDao.getNextSprNum(year);
    }

}
