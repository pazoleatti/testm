package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.HashMap;
import java.util.List;

/**
 * User: avanteev
 *
 * Дао для работы с файловым хранилищем.
 */
@Repository
public class BlobDataDaoImpl extends AbstractDao implements BlobDataDao {

    private static final class BlobDataRowMapper implements RowMapper<BlobData>{

        @Override
        public BlobData mapRow(ResultSet rs, int rowNum) throws SQLException {
            BlobData blobData = new BlobData();
            blobData.setCreationDate(rs.getTimestamp("creation_date"));
            blobData.setName(rs.getString("name"));
            blobData.setUuid(rs.getString("id"));
            blobData.setInputStream(rs.getBlob("data").getBinaryStream());
            return blobData;
        }
    }

    @Override
    public String create(final BlobData blobData) {
        try{
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "INSERT INTO blob_data (id, name, data, creation_date) VALUES (?,?,?,sysdate)");
                    ps.setString(1, blobData.getUuid());
                    ps.setString(2, blobData.getName());
                    ps.setBlob(3, blobData.getInputStream());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
            return blobData.getUuid();
        } catch (DataAccessException e) {
                throw new DaoException("Не удалось создать отчет." + e.toString());
        }
    }

    @Override
    public void delete(String uuid) {
        try{
            getJdbcTemplate().update("DELETE FROM blob_data WHERE id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR});
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить запись с id = %s", uuid), e);
        }
    }

    @Override
    public void delete(List<String> uuidStrings) {
        try {
            HashMap<String, Object> valuesMap = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(String.format("DELETE FROM blob_data WHERE %s", SqlUtils.transformToSqlInStatementForString("id", uuidStrings)),
                    valuesMap);
        } catch (DataAccessException e){
            logger.error(String.format("Не удалось удалить записи с id = %s", uuidStrings), e);
            throw new DaoException(String.format("Не удалось удалить записи с id = %s", uuidStrings), e);
        }
    }

    @Override
    public void save(final BlobData blobData) {
        try{
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "UPDATE blob_data SET data = ? WHERE id = ?");
                    ps.setBlob(1, blobData.getInputStream());
                    ps.setString(2, blobData.getUuid());
                    return ps;
                }
            };
            int rowNum = getJdbcTemplate().update(psc);
            if(rowNum == 0)
                throw new DaoException(String.format("Не существует записи с id = %s", blobData.getUuid()));

        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось обновить данные для id = %s", blobData.getUuid()), e);
        }
    }

    @Override
    public BlobData get(String uuid) {
        try{
            return getJdbcTemplate().queryForObject("SELECT id, name, data, creation_date FROM blob_data WHERE id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR},
                    new BlobDataRowMapper());
        }catch (EmptyResultDataAccessException e){
            return null;
        }
    }

    @Override
    public int clean() {
        try {
            return getJdbcTemplate().update("delete from blob_data bd where id not in " +
                    "(select distinct id from " +
                    "(select script_id id from ref_book " +
                    "union select xsd from declaration_template " +
                    "union select jrxml from declaration_template " +
                    "union select jasper_print from declaration_data " +
                    "union select data from declaration_data " +
                    "union select data_pdf from declaration_data " +
                    "union select data_xlsx from declaration_data " +
                    "union select blob_data_id from log_system) where id is not null) " +
                    "and (sysdate - bd.creation_date) > " + (isDateDiffNumber() ? "1" : "numtodsinterval(24, 'hour')"));
        } catch (DataAccessException e){
            throw new DaoException(String.format("Ошибка при удалении устаревших записей таблицы BLOB_DATA. %s.", e.getMessage()));
        }
    }
}
