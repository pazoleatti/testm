package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

/**
 * User: avanteev
 */
@Service
@Transactional
public class BlobDataServiceImpl implements BlobDataService {

    @Autowired
    BlobDataDao blobDataDao;

    @Override
    public String create(InputStream is, String name) {
        BlobData blobData = initBlob("", is, name, 0);
        return blobDataDao.create(blobData);
    }

    @Override
    public String createTemporary(InputStream is, String name) {
        BlobData blobData = initBlob("", is, name, 1);
        return blobDataDao.create(blobData);
    }

    @Override
    public void delete(String blob_id) {
        blobDataDao.delete(blob_id);
    }

    @Override
    public void save(String blob_id, InputStream is) {
        blobDataDao.save(initBlob(blob_id, is, "", 0));
    }

    @Override
    public InputStream get(String blob_id) {
        BlobData blobData = blobDataDao.get(blob_id);
        return blobData.getInputStream();
    }

    private BlobData initBlob(String blob_id, InputStream is, String name, int isTemp){
        BlobData blobData = new BlobData();
        blobData.setName(name);
        blobData.setInputStream(is);
        blobData.setCreationDate(new Date());
        blobData.setUuid(blob_id.isEmpty() ? UUID.randomUUID().toString().toLowerCase() : blob_id);
        blobData.setType(isTemp);
        return blobData;
    }
}
