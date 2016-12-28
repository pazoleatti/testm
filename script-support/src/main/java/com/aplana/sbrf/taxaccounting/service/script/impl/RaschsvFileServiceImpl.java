package com.aplana.sbrf.taxaccounting.service.script.impl;

import com.aplana.sbrf.taxaccounting.dao.raschsv.RaschsvFileDao;
import com.aplana.sbrf.taxaccounting.model.raschsv.RaschsvFile;
import com.aplana.sbrf.taxaccounting.service.script.RaschsvFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("raschsvFileService")
public class RaschsvFileServiceImpl implements RaschsvFileService {

    @Autowired
    private RaschsvFileDao raschsvFileDao;

    @Override
    public Integer insert(RaschsvFile raschsvFile) {
        return raschsvFileDao.insert(raschsvFile);
    }
}
