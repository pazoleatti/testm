package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("blobDataServiceDaoImpl")
public class BlobDataServiceImpl implements BlobDataService {

    @Autowired
    private BlobDataDao blobDataDao;

    @Override
    public BlobData get(String uuid) {
        return blobDataDao.get(uuid);
    }
}
