package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

@Repository
@Transactional(readOnly=true)
public class ReportDaoImpl extends AbstractDao implements ReportDao {

	private static final Log LOG = LogFactory.getLog(ReportDaoImpl.class);

    @Override
    public void create(final long formDataId, final String blobDataId, final String type, final boolean checking, final boolean manual, final boolean absolute) {
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
                    ps.setString(3, type);
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
    public void createAudit(Integer userId, String blobDataId, ReportType type) {
        try{
            getJdbcTemplate().update(
                    "INSERT INTO LOG_SYSTEM_REPORT (SEC_USER_ID, BLOB_DATA_ID, TYPE) VALUES (?,?,?)",
                    userId, blobDataId, type.getId());
        } catch (DataIntegrityViolationException e){
			LOG.error("", e);
            throw new DaoException("Возможно для этого пользователя уже есть отчет по ЖА, проверьте выгрузку.", e);
        }
    }

    @Override
    public String get(final long formDataId, final String type, final boolean checking, final boolean manual, final boolean absolute) {
        try{
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("SELECT BLOB_DATA_ID FROM FORM_DATA_REPORT " +
                            "WHERE FORM_DATA_ID = ? AND TYPE = ? AND CHECKING = ? AND MANUAL = ? AND ABSOLUTE = ?");
            ps.addParam(formDataId);
            ps.addParam(type);
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
    public String getAudit(Integer userId, ReportType type) {
        try{
            String sql = (userId != null ?
                    "select blob_data_id from log_system_report where type=? and sec_user_id=?" :
                    "select blob_data_id from log_system_report where type=? and sec_user_id is null");
            Object[] objects = (userId != null ? new Object[] {type.getId(), userId} : new Object[] { type.getId() });
            return getJdbcTemplate().queryForObject(
                    sql,
                    objects,
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
    public void deleteDec(final Collection<Long> declarationDataIds) {
        try {
            String sql = String.format("DELETE FROM DECLARATION_REPORT WHERE %s", SqlUtils.transformToSqlInStatement("DECLARATION_DATA_ID", declarationDataIds));
            Map<String, Object> params = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException e){
            throw new DaoException("Не удалось удалить записи", e);
        }
    }

    @Override
    public void deleteDec(Collection<Long> declarationDataIds, List<ReportType> reportTypes) {
        try{
            List<Integer> types = new ArrayList<Integer>();
            for (ReportType type : reportTypes) {
                types.add(type.getId());
            }
            String sql = String.format("DELETE FROM DECLARATION_REPORT WHERE %s and %s",
                    SqlUtils.transformToSqlInStatement("DECLARATION_DATA_ID", declarationDataIds),
                    SqlUtils.transformToSqlInStatement("TYPE", types));
            Map<String, Object> params = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException e){
            throw new DaoException("Не удалось удалить записи", e);
        }
    }


    @Override
    public void deleteDec(String uuid) {
        try{
            getJdbcTemplate().update("DELETE FROM DECLARATION_REPORT WHERE BLOB_DATA_ID = ?",
                    new Object[]{uuid},
                    new int[]{Types.VARCHAR});
        } catch (DataAccessException e){
            throw new DaoException(String.format("Не удалось удалить записи с BLOB_DATA_ID = %s", uuid), e);
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

    @Override
    public int clean() {
        try {
            //Удаление Jasper-отчетов декларации, если есть сформированный XLSX-отчет
            return getJdbcTemplate().update("delete from declaration_report dr\n" +
                    "where type = 3 and exists ( \n" +
                    "select declaration_data_id\n" +
                    "from declaration_report dr1\n" +
                    "where dr.declaration_data_id=dr1.declaration_data_id and type = 0)");
        } catch (DataAccessException e){
            throw new DaoException(String.format("Ошибка при удалении ненужных записей таблицы DECLARATION_REPORT. %s.", e.getMessage()), e);
        }
    }
}
