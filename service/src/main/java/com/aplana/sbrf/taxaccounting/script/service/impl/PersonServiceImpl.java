package com.aplana.sbrf.taxaccounting.script.service.impl;


import com.aplana.sbrf.taxaccounting.dao.PersonDao;
import com.aplana.sbrf.taxaccounting.script.service.PersonService;
import com.aplana.sbrf.taxaccounting.service.ScriptComponentContext;
import com.aplana.sbrf.taxaccounting.service.ScriptComponentContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("personService")
public class PersonServiceImpl implements PersonService{

    @Autowired
    private PersonDao personDao;

    @Override
    public List<Long> getDuplicate(Set<Long> originalRecordIds) {
        return personDao.getDuplicateIds(originalRecordIds);
    }
}
