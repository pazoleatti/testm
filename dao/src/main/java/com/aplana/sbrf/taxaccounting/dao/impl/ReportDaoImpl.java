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
import java.util.HashMap;
import java.util.Map;

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
                                    "INSERT INTO FORM_DATA_REPORT (FORM_DATA_ID, BLOB_DATA_ID, TYPE, CHECKING, MANUAL, ABSOLUTE) VALUES (?,?,?,?,?,?)");
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
    public void createDec(final long declarationDataId, final String blobDataId, final ReportType type) {
        try{
            PreparedStatementCreator psc = new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    PreparedStatement ps = con
                            .prepareStatement(
                                    "INSERT INTO DECLARATION_REPORT (DECLARATION_DATA_ID, BLOB_DATA_ID, TYPE) VALUES (?,?,?)");
                    ps.setLong(1, declarationDataId);
                    ps.setString(2, blobDataId);
                    ps.setInt(3, type.getId());
                    return ps;
                }
            };
            getJdbcTemplate().update(psc);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось записать данные." + e.toString());
        }
    }

    @Override
    public void createAudit(int userId, String blobDataId, ReportType type) {
        getJdbcTemplate().update(
                "INSERT INTO LOG_SYSTEM_REPORT (SEC_USER_ID, BLOB_DATA_ID, TYPE) VALUES (?,?,?)",
                userId, blobDataId, type.getId());
    }

    @Override
    public String get(final long formDataId, final ReportType type, final boolean checking, final boolean manual, final boolean absolute) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("SELECT BLOB_DATA_ID FROM FORM_DATA_REPORT " +
                            "WHERE FORM_DATA_ID = ? AND TYPE = ? AND CHECKING = ? AND MANUAL = ? AND ABSOLUTE = ?");
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
    public String getDec(final long declarationDataId, final ReportType type) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("SELECT BLOB_DATA_ID FROM DECLARATION_REPORT " +
                    "WHERE DECLARATION_DATA_ID = ? AND TYPE = ?");
            ps.addParam(declarationDataId);
            ps.addParam(type.getId());
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось получить данные." + e.toString());
        }
    }

    @Override
    public String getAudit(int userId, ReportType type) {
        try{
            return getJdbcTemplate().queryForObject(
                    "select blob_data_id from log_system_report where type=? and sec_user_id=?",
                    new Object[]{type.getId(), userId},
                    String.class);
        } catch (EmptyResultDataAccessException e){
            return null;
        } catch (DataAccessException e){
            throw new DaoException("Не удалось получить отчет ЖА", e);
        }
    }

    @Override
    public void delete(long formDataId, Boolean manual) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("formDataId", formDataId);
            params.put("manual", manual);
            getNamedParameterJdbcTemplate().update("DELETE FROM FORM_DATA_REPORT WHERE FORM_DATA_ID = :formDataId and (:manual IS NULL OR MANUAL = :manual)",
                    params);
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с form_data_id = %d", formDataId), e);
        }
    }


    @Override
    public void deleteDec(long declarationDataId) {
        try{
            getJdbcTemplate().update("DELETE FROM DECLARATION_REPORT WHERE DECLARATION_DATA_ID = ?",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с declaration_data_id = %d", declarationDataId), e);
        }
    }

    @Override
    public void deleteAudit(int userId, ReportType reportType) {
        try{
            getJdbcTemplate().update("delete from log_system_report where type=? and sec_user_id=?",
                    userId, reportType.getId());
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи ЖА пользователя с идентификатором %d ", userId), e);
        }
    }

    @Override
    public void deleteAudit(String blobDataId) {
        try{
            getJdbcTemplate().update("delete from log_system_report where blob_data_id=?", blobDataId);
        } catch (DataAccessException e){
            throw new DaoException("Не удалось удалить записи ЖА", e);
        }
    }
}
