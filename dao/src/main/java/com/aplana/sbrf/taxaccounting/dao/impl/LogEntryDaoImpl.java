package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.LogEntryDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.sql.*;
import java.util.List;

@Repository
public class LogEntryDaoImpl extends AbstractDao implements LogEntryDao {

    private static final class LogEntriesRowMapper implements RowMapper<List<LogEntry>> {

        @Override
        public List<LogEntry> mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                InputStream is = rs.getBlob("data").getBinaryStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                return (List<LogEntry>) ois.readObject();
            } catch (Exception e) {
                throw new DaoException(e.getMessage(), e);
            }
        }
    }

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
            blobData.setType(0);
            blobData.setDataSize(baos.size());

            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "insert into blob_data (id, name, data, creation_date, type, data_size) values (?,?,?,?,?,?)");
                    ps.setString(1, blobData.getUuid());
                    ps.setString(2, blobData.getName());
                    ps.setBlob(3, blobData.getInputStream());
                    ps.setDate(4, new Date(blobData.getCreationDate().getTime()));
                    ps.setInt(5, blobData.getType());
                    ps.setInt(6, blobData.getDataSize());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
        } catch (Exception e) {
            throw new DaoException("Не удалось создать запись. " + e.getMessage());
        }
    }

    @Override
    @Cacheable(value = "DataBlobsCache", key = "#uuid")
    public List<LogEntry> get(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return null;
        }
        try {
            return getJdbcTemplate().queryForObject("select data from blob_data where id = ?",
                    new Object[]{uuid},
                    new LogEntriesRowMapper());
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(String.format("Не удалось получить запись с id = %s", uuid), e);
        }
    }
}
