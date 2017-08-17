package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.LogDao;
import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import com.aplana.sbrf.taxaccounting.service.BlobDataService;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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

    @Autowired
    private ReportDao reportDao;

    @Autowired
    private LogDao logDao;

    @Override
    public String create(InputStream is, String name) {
        BlobData blobData = initBlob("", is, name, null);
        return blobDataDao.createWithSysdate(blobData);
    }

    @Override
    public String create(String path, String name) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(path);
            return create(fileInputStream, name);
        } catch (FileNotFoundException e) {
            throw new ServiceException("", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    @Override
    public String create(File file, String name, LocalDateTime createDate) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            BlobData data = initBlob("", fileInputStream, name, createDate);
            return blobDataDao.create(data);
        } catch (FileNotFoundException e) {
            throw new ServiceException("", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    @Override
    public String create(BlobData data) {
        return blobDataDao.createWithSysdate(data);
    }

    @Override
    public void delete(String blobId) {
        try {
            blobDataDao.delete(blobId);
        } catch (DaoException e) {
            throw new ServiceException("Ошибка при удалении записи.", e);
        }
    }

    @Override
    public void delete(List<String> blobIdStrings) {
        try {
            blobDataDao.delete(blobIdStrings);
        } catch (DaoException e) {
            throw new ServiceException("Ошибка при удалении записей.", e);
        }
    }

    @Override
    public void save(String blobId, InputStream is) {
        blobDataDao.updateDataByUUID(blobId, is);
    }

    @Override
    public BlobData get(String blobId) {
        return blobDataDao.fetch(blobId);
    }

    private static BlobData initBlob(String blobId, InputStream is, String name, LocalDateTime date) {
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

    @Override
    public long getLength(String uuid) {
        return blobDataDao.fetchLength(uuid);
    }

    @Override
    public long clean() {
        reportDao.clean();
        logDao.clean();
        return blobDataDao.clean();
    }
}
