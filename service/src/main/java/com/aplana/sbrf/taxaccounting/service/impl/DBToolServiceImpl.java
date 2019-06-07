package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.DBToolsDao;
import com.aplana.sbrf.taxaccounting.service.DBToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBToolServiceImpl implements DBToolService{
    @Autowired
    DBToolsDao dbToolsDao;

    @Override
    public void shrinkTables() {
        dbToolsDao.shrinkTables();
    }
}
