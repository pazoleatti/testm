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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
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

        ObjectOutput out = null;
        InputStream in  = null;
        File file = null;
        try {
            final BlobData blobData = new BlobData();
            file = File.createTempFile("log_entry_", ".tmp");
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeObject(logEntries);
            out.flush();

            in = new BufferedInputStream(new FileInputStream(file));
            blobData.setInputStream(in);
            blobData.setUuid(uuid);
            blobData.setCreationDate(new java.util.Date());

            blobDataDao.create(blobData);
        } catch (Exception e) {
            throw new DaoException("Не удалось создать запись. " + e.getMessage());
        } finally {
            try {
                out.close();
                in.close();
                file.delete();
            } catch (Exception e) {
            }
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
                ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(is));
                return (List<LogEntry>) ois.readObject();
            } catch (Exception e) {
                throw new DaoException(String.format("Не удалось получить запись с id = %s", uuid), e);
            }
        } else {
            throw new DaoException(String.format("Не удалось получить запись с id = %s", uuid));
        }
    }

    @Override
    @CacheEvict(value = "DataBlobsCache", key = "#uuid", beforeInvocation = true)
    public void update(List<LogEntry> logEntries, String uuid) {
        if (logEntries == null || logEntries.isEmpty() || uuid == null || uuid.isEmpty()) {
            return;
        }

        ObjectOutput out = null;
        InputStream in  = null;
        File file = null;
        try {
            file = File.createTempFile("log_entry_", ".tmp");
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            out.writeObject(logEntries);
            out.flush();

            in = new BufferedInputStream(new FileInputStream(file));
            blobDataDao.save(uuid, in);
        } catch (Exception e) {
            throw new DaoException("Не удалось создать запись. " + e.getMessage());
        } finally {
            try {
                out.close();
                in.close();
                file.delete();
            } catch (Exception e) {
            }
        }
    }
}
