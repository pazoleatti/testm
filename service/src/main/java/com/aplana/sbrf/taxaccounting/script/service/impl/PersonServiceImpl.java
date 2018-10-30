package com.aplana.sbrf.taxaccounting.script.service.impl;


import com.aplana.sbrf.taxaccounting.dao.IdDocDao;
import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookPersonDao;
import com.aplana.sbrf.taxaccounting.model.identification.NaturalPerson;
import com.aplana.sbrf.taxaccounting.model.refbook.RegistryPerson;
import com.aplana.sbrf.taxaccounting.script.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service("personService")
public class PersonServiceImpl implements PersonService{

    @Autowired
    private RefBookPersonDao refBookPersonDao;
    @Autowired
    private com.aplana.sbrf.taxaccounting.service.impl.PersonServiceImpl personService;
    @Autowired
    private IdDocDao idDocDao;

    @Override
    public List<Long> getDuplicate(Set<Long> originalRecordIds) {
        return refBookPersonDao.getDuplicateIds(originalRecordIds);
    }

    @Override
    public int getCountOfUniqueEntries(long declarationDataId) {
        return refBookPersonDao.getCountOfUniqueEntries(declarationDataId);
    }

    @Override
    public List<RegistryPerson> saveNewIdentificatedPersons(List<NaturalPerson> persons) {
        List<RegistryPerson> toSave = new ArrayList<>();
        for (NaturalPerson person : persons) {
            toSave.add(person);
        }
        return personService.savePersons(toSave);
    }

    @Override
    public List<RegistryPerson> savePersons(List<RegistryPerson> persons) {
        return personService.savePersons(persons);
    }

    @Override
    public void updatePersons(List<NaturalPerson> persons) {
        personService.updateIdentificatedPersons(persons);
    }

    @Override
    public List<RegistryPerson> findActualRefPersonsByDeclarationDataId(Long declarationDataId) {
        return personService.findActualRefPersonsByDeclarationDataId(declarationDataId);
    }

    @Override
    public int findIdDocCount(Long personRecordId) {
        return idDocDao.findIdDocCount(personRecordId);
    }
}
