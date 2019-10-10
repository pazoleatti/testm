package com.aplana.sbrf.taxaccounting.script.service.impl;

import com.aplana.sbrf.taxaccounting.dao.refbook.NdflReferenceDao;
import com.aplana.sbrf.taxaccounting.model.refbook.NumFor2Ndfl;
import com.aplana.sbrf.taxaccounting.script.service.NdflReferenceService;
import com.aplana.sbrf.taxaccounting.service.TransactionHelper;
import com.aplana.sbrf.taxaccounting.service.TransactionLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("ndflReferenceService")
public class NdflReferenceServiceImpl implements NdflReferenceService {

    @Autowired
    private NdflReferenceDao ndflReferenceDao;
    @Autowired
    private TransactionHelper tx;

    @Override
    public List<NumFor2Ndfl> getCorrSprNum(Long personId, int year, String kpp, String oktmo, int declarationTypeId) {
        return ndflReferenceDao.getCorrSprNum(personId, year, kpp, oktmo, declarationTypeId);
    }

    @Override
    public Integer getNextSprNum(final Integer year) {
        Integer sequenceCount = ndflReferenceDao.countSequenceByYear(year);
        if (sequenceCount == 0) {
            tx.executeInNewTransaction(new TransactionLogic() {
                @Override
                public Object execute() {
                    ndflReferenceDao.createSequence(year);
                    return null;
                }
            });
        }
        return ndflReferenceDao.getNextSprNum(year);
    }

}
