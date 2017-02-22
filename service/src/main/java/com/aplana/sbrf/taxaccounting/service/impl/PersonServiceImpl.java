package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.PersonDao;
import com.aplana.sbrf.taxaccounting.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.aplana.sbrf.taxaccounting.model.TaxType.ETR;

/**
 * Сервис работы с периодами
 *
 * Только этот сервис должен использоваться для работы с отчетными и налоговыми периодами
 *
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService {

    @Autowired
    private PersonDao personDao;

    @Override
    public Long getOriginal(Long recordId) {
        return personDao.getOriginal(recordId);
    }

    @Override
    public List<Long> getDuplicate(Long recordId) {
        return personDao.getDuplicate(recordId);
    }

    @Override
    public void setOriginal(List<Long> recordIds) {
        personDao.setOriginal(recordIds);
    }

    @Override
    public void setDuplicate(List<Long> recordIds, Long originalId) {
        personDao.setDuplicate(recordIds, originalId);
    }

    @Override
    public void changeRecordId(List<Long> recordIds, Long originalId) {
        personDao.changeRecordId(recordIds, originalId);
    }
}
