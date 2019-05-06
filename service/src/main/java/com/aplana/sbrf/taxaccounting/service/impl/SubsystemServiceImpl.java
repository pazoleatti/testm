package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.SubsystemDao;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.Subsystem;
import com.aplana.sbrf.taxaccounting.service.SubsystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Сервис для справочника "Подсистемы АС УН".
 */
@Service
public class SubsystemServiceImpl implements SubsystemService {

    @Autowired
    private SubsystemDao subsystemDao;

    @Override
    public PagingResult<Subsystem> findByName(String name) {
        return subsystemDao.findByName(name);
    }
}
