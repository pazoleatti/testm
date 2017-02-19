package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

@Service("blobDataServiceDaoImpl")
public class BlobDataServiceImpl implements BlobDataService {

    @Autowired
    private BlobDataDao blobDataDao;

    @Autowired
    private com.aplana.sbrf.taxaccounting.service.BlobDataService blobDataService;

    @Override
    public BlobData get(String uuid) {
        return blobDataDao.get(uuid);
    }

    @Override
    public String create(File file, String name, Date createDate) {
        return blobDataService.create(file, name, createDate);
    }
}
