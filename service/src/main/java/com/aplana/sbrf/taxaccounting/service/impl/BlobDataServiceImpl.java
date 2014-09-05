package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
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
        BlobData blobData = initBlob("", is, name);
        return blobDataDao.create(blobData);
    }

    @Override
    public void delete(String blobId) {
        blobDataDao.delete(blobId);
    }

    @Override
    public void delete(List<String> blobIdStrings) {
        try {
            blobDataDao.delete(blobIdStrings);
        } catch (DaoException e){
            throw new ServiceException("Ошибка при удалении записей.", e);
        }
    }

    @Override
    public void save(String blobId, InputStream is) {
        blobDataDao.save(initBlob(blobId, is, ""));
    }

    @Override
    public BlobData get(String blobId) {
        return blobDataDao.get(blobId);
    }

    private BlobData initBlob(String blobId, InputStream is, String name){
        BlobData blobData = new BlobData();
        blobData.setName(name);
        try {
            is.available();
        } catch (IOException e) {
            throw new ServiceException("Ошибка при получении данных", e);
        }
        blobData.setInputStream(is);
        blobData.setCreationDate(new Date());
        blobData.setUuid(blobId.isEmpty() ? UUID.randomUUID().toString().toLowerCase() : blobId);
        return blobData;
    }
}
