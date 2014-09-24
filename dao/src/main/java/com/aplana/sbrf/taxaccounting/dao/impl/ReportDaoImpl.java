package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.List;

@Repository
@Transactional(readOnly=true)
public class ReportDaoImpl extends AbstractDao implements ReportDao {
    @Override
    public void create(final long formDataId, final String blobDataId, final ReportType type, final boolean checking, final boolean manual, final boolean absolute) {
        try{
            PreparedStatementCreator psc = new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "INSERT INTO REPORT (FORM_DATA_ID, BLOB_DATA_ID, TYPE, CHECKING, MANUAL, ABSOLUTE) VALUES (?,?,?,?,?,?)");
                    ps.setLong(1, formDataId);
                    ps.setString(2, blobDataId);
                    ps.setInt(3, type.getId());
                    ps.setBoolean(4, checking);
                    ps.setBoolean(5, manual);
                    ps.setBoolean(6, absolute);
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось записать данные." + e.toString());
        }
    }

    @Override
    public String get(final long formDataId, final ReportType type, final boolean checking, final boolean manual, final boolean absolute) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("select BLOB_DATA_ID from report " +
                            "where FORM_DATA_ID = ? and TYPE = ? and CHECKING = ? and MANUAL = ? and ABSOLUTE = ?");
            ps.addParam(formDataId);
            ps.addParam(type.getId());
            ps.addParam(checking);
            ps.addParam(manual);
            ps.addParam(absolute);
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось получить данные." + e.toString());
        }
    }

    @Override
    public void delete(long formDataId) {
        try{
            getJdbcTemplate().update("DELETE FROM REPORT WHERE form_data_id = ?",
                    new Object[]{formDataId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с form_data_id = %d", formDataId), e);
        }
    }
}
