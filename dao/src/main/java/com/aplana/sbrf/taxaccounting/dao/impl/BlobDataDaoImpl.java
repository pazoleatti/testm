package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.BlobDataDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.BlobData;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;

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
            blobData.setType(rs.getInt("type"));
            blobData.setCreationDate(rs.getDate("creation_date"));
            blobData.setName(rs.getString("name"));
            blobData.setUuid(rs.getString("id"));
            blobData.setInputStream(rs.getBlob("data").getBinaryStream());
            blobData.setDataSize(rs.getInt("data_size"));
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
                                    "insert into blob_data (id, name, data, creation_date, type, data_size) values (?,?,?,?,?,?)");
                    ps.setString(1, blobData.getUuid());
                    ps.setString(2, blobData.getName());
                    ps.setBlob(3, blobData.getInputStream());
                    ps.setDate(4, new java.sql.Date(blobData.getCreationDate().getTime()));
                    ps.setInt(5, blobData.getType());
                    ps.setInt(6, blobData.getDataSize());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
            return blobData.getUuid();
        }catch (DataAccessException e) {
                throw new DaoException("Не удалось создать отчет." + e.toString());
        }
    }

    @Override
    /*@CacheEvict(value = "DataBlobsCache", key = "#uuid", beforeInvocation = true)*/
    public void delete(String uuid) {
        try{
            getJdbcTemplate().update("delete from blob_data where id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR});
        }catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить запись с id = %s", uuid), e);
        }
    }

    @Override
    /*@CacheEvict(value = "DataBlobsCache", key = "#blobData.uuid", beforeInvocation = true)*/
    public void save(final BlobData blobData) {
        try{
            PreparedStatementCreator psc = new PreparedStatementCreator() {

                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "update blob_data set data = ? where id = ?");
                    ps.setBlob(1, blobData.getInputStream());
                    ps.setString(2, blobData.getUuid());
                    return ps;
                }
            };
            int rowNum = getJdbcTemplate().update(psc);
            if(rowNum == 0)
                throw new DaoException(String.format("Не существует записи с id = %s", blobData.getUuid()));

        }catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось обновить данные для id = %s", blobData.getUuid()), e);
        }
    }

    @Override
    /*@Cacheable("DataBlobsCache")*/
    public BlobData get(String uuid) {
        try{
            return getJdbcTemplate().queryForObject("select * from blob_data where id = ?",
                    new Object[]{uuid},
                    new int[]{Types.CHAR},
                    new BlobDataRowMapper());
        }catch (EmptyResultDataAccessException e){
            /*throw new DaoException(String.format("Не удалось найти запись с id = %s", uuid), e);*/
            return null;
        }
    }

}
