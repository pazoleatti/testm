package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.List;

@Repository
public class LogEntryDaoImpl extends AbstractDao implements LogEntryDao {

    @Autowired
    BlobDataDao blobDataDao;

    @Override
    @CacheEvict(value = "DataBlobsCache", key = "#uuid", beforeInvocation = true)
    public void save(List<LogEntry> logEntries, String uuid) {
        if (logEntries == null || logEntries.isEmpty() || uuid == null || uuid.isEmpty()) {
            return;
        }
        try {
            final BlobData blobData = new BlobData();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(baos);
            output.writeObject(logEntries);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());

            blobData.setInputStream(is);
            blobData.setUuid(uuid);
            blobData.setCreationDate(new java.util.Date());

            blobDataDao.create(blobData);
        } catch (Exception e) {
            throw new DaoException("Не удалось создать запись. " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "DataBlobsCache", key = "#uuid")
    public List<LogEntry> get(String uuid) {
        if (uuid.isEmpty()) {
            return null;
        }

        BlobData blobData = blobDataDao.get(uuid);
        if (blobData != null) {
            InputStream is = blobData.getInputStream();
            try {
                ObjectInputStream ois = new ObjectInputStream(is);
                return (List<LogEntry>) ois.readObject();
            } catch (Exception e) {
                throw new DaoException(String.format("Не удалось получить запись с id = %s", uuid), e);
            }
        } else {
            throw new DaoException(String.format("Не удалось получить запись с id = %s", uuid));
        }
    }

    @Override
    public void update(List<LogEntry> logEntries, String uuid) {
        if (logEntries == null || logEntries.isEmpty() || uuid == null || uuid.isEmpty()) {
            return;
        }
        try {
            final BlobData blobData = new BlobData();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(baos);
            output.writeObject(logEntries);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());

            blobData.setInputStream(is);
            blobData.setUuid(uuid);

            blobDataDao.save(blobData);
        } catch (Exception e) {
            throw new DaoException("Не удалось обновить запись. " + e.getMessage());
        }
    }
}
