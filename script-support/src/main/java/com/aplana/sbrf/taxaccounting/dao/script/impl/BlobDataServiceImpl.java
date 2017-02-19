package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.script.BlobDataService;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Date;
import java.util.UUID;

@Service("blobDataServiceDaoImpl")
public class BlobDataServiceImpl implements BlobDataService {

    @Autowired
    private BlobDataDao blobDataDao;

    @Override
    public BlobData get(String uuid) {
        return blobDataDao.get(uuid);
    }

    @Override
    public String create(File file, String name, Date createDate) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            BlobData data = initBlob("", fileInputStream, name, createDate);
            return blobDataDao.createWithDate(data);
        } catch (FileNotFoundException e) {
            throw new ServiceException("", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    private static BlobData initBlob(String blobId, InputStream is, String name, Date date){
        BlobData blobData = new BlobData();
        blobData.setName(name);
        try {
            is.available();
        } catch (IOException e) {
            throw new ServiceException("Ошибка при получении данных", e);
        }
        blobData.setInputStream(is);
        blobData.setCreationDate(date);
        blobData.setUuid(blobId.isEmpty() ? UUID.randomUUID().toString().toLowerCase() : blobId);
        return blobData;
    }
}
