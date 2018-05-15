package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Реализация доступа к базе данных для {@link BlobData}.
 */
@Repository
public class BlobDataDaoImpl extends AbstractDao implements BlobDataDao {

    private static final Log LOG = LogFactory.getLog(BlobDataDaoImpl.class);

    @Override
    public String createWithSysdate(final BlobData newBlobData) {
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "INSERT INTO blob_data (id, name, data, creation_date) VALUES (?,?,?,sysdate)");
                    ps.setString(1, newBlobData.getUuid());
                    ps.setString(2, newBlobData.getName());
                    ps.setBlob(3, newBlobData.getInputStream());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
            return newBlobData.getUuid();
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось создать отчет." + e.toString());
        }
    }

    @Override
    public String create(final BlobData newBlobData) {
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "INSERT INTO blob_data (id, name, creation_date, data) VALUES (?,?,?,?)");
                    ps.setString(1, newBlobData.getUuid());
                    ps.setString(2, newBlobData.getName());
                    ps.setTimestamp(3, new java.sql.Timestamp(newBlobData.getCreationDate().getTime()));
                    ps.setBlob(4, newBlobData.getInputStream());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
            return newBlobData.getUuid();
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось создать отчет." + e.toString());
        }
    }

    @Override
    public void delete(String uuid) {
        try {
            getJdbcTemplate().update("DELETE FROM blob_data WHERE id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR});
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Не удалось удалить запись с id = %s", uuid), e);
        }
    }

    @Override
    public void delete(List<String> uuidStrings) {
        try {
            HashMap<String, Object> valuesMap = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(String.format("DELETE FROM blob_data WHERE %s", SqlUtils.transformToSqlInStatementForString("id", uuidStrings)),
                    valuesMap);
        } catch (DataAccessException e) {
            LOG.error(String.format("Не удалось удалить записи с id = %s", uuidStrings), e);
            throw new DaoException(String.format("Не удалось удалить записи с id = %s", uuidStrings), e);
        }
    }

    @Override
    public void updateDataByUUID(final String uuid, final InputStream inputStream) {
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "UPDATE blob_data SET data = ? WHERE id = ?");
                    ps.setBlob(1, inputStream);
                    ps.setString(2, uuid);
                    return ps;
                }
            };
            int rowNum = getJdbcTemplate().update(psc);
            if (rowNum == 0)
                throw new DaoException(String.format("Не существует записи с id = %s", uuid));

        } catch (DataAccessException e) {
            throw new DaoException(String.format("Не удалось обновить данные для id = %s", uuid), e);
        }
    }

    @Override
    public BlobData fetch(String uuid) {
        try {
            return getJdbcTemplate().queryForObject("SELECT id, name, data, creation_date FROM blob_data WHERE id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR},
                    new BlobDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public long fetchLength(String uuid) {
        try {
            return getJdbcTemplate().queryForObject("SELECT dbms_lob.getlength(data) FROM blob_data WHERE id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR},
                    Long.class);
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public long clean() {
        try {
            return getJdbcTemplate().update("delete from blob_data bd where id not in " +
                    "(select distinct id from " +
                    "(select script_id id from ref_book " +
                    "union select xsd_id from ref_book " +
                    "union select xsd from declaration_template " +
                    "union select jrxml from declaration_template " +
                    "union select blob_data_id from declaration_report " +
                    "union select blob_data_id from declaration_data_file " +
                    "union select blob_data_id from declaration_template_file " +
                    "union select blob_data_id from declaration_subreport) where id is not null) " +
                    "and (sysdate - bd.creation_date) > " + (isDateDiffNumber() ? "1" : "numtodsinterval(24, 'hour')"));
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Ошибка при удалении устаревших записей таблицы BLOB_DATA. %s.", e.getMessage()));
        }
    }

    private static final class BlobDataRowMapper implements RowMapper<BlobData> {

        @Override
        public BlobData mapRow(ResultSet rs, int rowNum) throws SQLException {
            BlobData blobData = new BlobData();
            blobData.setCreationDate(new Date(rs.getTimestamp("creation_date").getTime()));
            blobData.setName(rs.getString("name"));
            blobData.setUuid(rs.getString("id"));
            blobData.setInputStream(rs.getBlob("data").getBinaryStream());
            return blobData;
        }
    }
}