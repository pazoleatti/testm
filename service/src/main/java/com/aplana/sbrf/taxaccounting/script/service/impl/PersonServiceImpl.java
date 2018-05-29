package com.aplana.sbrf.taxaccounting.script.service.impl;


import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.script.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("personService")
public class PersonServiceImpl implements PersonService{

    @Autowired
    private RefBookPersonDao refBookPersonDao;

    @Override
    public List<Long> getDuplicate(Set<Long> originalRecordIds) {
        return refBookPersonDao.getDuplicateIds(originalRecordIds);
    }

    @Override
    public int getCountOfUniqueEntries(long declarationDataId) {
        return refBookPersonDao.getCountOfUniqueEntries(declarationDataId);
    }
}
