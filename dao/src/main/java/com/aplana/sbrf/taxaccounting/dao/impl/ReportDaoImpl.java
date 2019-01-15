package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.ReportDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(readOnly = true)
public class ReportDaoImpl extends AbstractDao implements ReportDao {

    private static final Log LOG = LogFactory.getLog(ReportDaoImpl.class);

    @Override
    public void createDec(final long declarationDataId, final String blobDataId, final DeclarationDataReportType type) {
        try {
            PreparedStatementCreator psc = new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con)
                        throws SQLException {

                    if (type.getSubreport() != null) {
                        PreparedStatement ps = con
                                .prepareStatement(
                                        "INSERT INTO DECLARATION_REPORT (DECLARATION_DATA_ID, BLOB_DATA_ID, TYPE, SUBREPORT_ID) VALUES (?,?,?,?)");
                        ps.setLong(1, declarationDataId);
                        ps.setString(2, blobDataId);
                        ps.setLong(3, type.getReportType().getId());
                        ps.setLong(4, type.getSubreport().getId());
                        return ps;
                    } else {
                        PreparedStatement ps = con
                                .prepareStatement(
                                        "INSERT INTO DECLARATION_REPORT (DECLARATION_DATA_ID, BLOB_DATA_ID, TYPE, SUBREPORT_ID) VALUES (?,?,?,null)");
                        ps.setLong(1, declarationDataId);
                        ps.setString(2, blobDataId);
                        ps.setLong(3, type.getReportType().getId());
                        return ps;
                    }
                }
            };
            getJdbcTemplate().update(psc);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось записать данные." + e.toString());
        }
    }

    @Override
    public String getDec(final long declarationDataId, final DeclarationDataReportType type) {
        try {
            PreparedStatementData ps = new PreparedStatementData();
            ps.appendQuery("SELECT BLOB_DATA_ID FROM DECLARATION_REPORT " +
                    "WHERE DECLARATION_DATA_ID = ? AND TYPE = ?");
            ps.addParam(declarationDataId);
            ps.addParam(type.getReportType().getId());
            if (type.getSubreport() != null) {
                ps.appendQuery("AND SUBREPORT_ID = ?");
                ps.addParam(type.getSubreport().getId());
            }

            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось получить данные." + e.toString());
        }
    }

    @Override
    public void deleteAllByDeclarationId(long declarationDataId) {
        try {
            getJdbcTemplate().update("DELETE FROM DECLARATION_REPORT WHERE DECLARATION_DATA_ID = ?",
                    new Object[]{declarationDataId},
                    new int[]{Types.INTEGER});
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Не удалось удалить записи с declaration_data_id = %d", declarationDataId), e);
        }
    }

    @Override
    public void deleteDec(final Collection<Long> declarationDataIds) {
        try {
            String sql = String.format("DELETE FROM DECLARATION_REPORT WHERE %s", SqlUtils.transformToSqlInStatement("DECLARATION_DATA_ID", declarationDataIds));
            Map<String, Object> params = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось удалить записи", e);
        }
    }

    @Override
    public void deleteDec(long declarationDataId, DeclarationDataReportType type) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("declarationDataId", declarationDataId);
            params.addValue("type", type.getReportType().getId());
            if (type.getSubreport() != null) {
                params.addValue("subreportId", type.getSubreport().getId());
            }
            getNamedParameterJdbcTemplate().update(
                    "DELETE FROM DECLARATION_REPORT WHERE DECLARATION_DATA_ID = :declarationDataId AND TYPE = :type" +
                            (type.getSubreport() != null ? " AND SUBREPORT_ID = :subreportId" : "")
                    , params);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось удалить записи", e);
        }
    }

    @Override
    public void deleteSubreport(long declarationDataId, String subreportAlias) {
        String query = "delete from declaration_report dr\n" +
                "where\n" +
                "dr.declaration_data_id = :declarationDataId\n" +
                "and dr.subreport_id in (select id from declaration_subreport where alias = :subreportAlias)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId)
                .addValue("subreportAlias", subreportAlias);
        getNamedParameterJdbcTemplate().update(query, params);
    }

    @Override
    public void deleteDec(Collection<Long> declarationDataIds, List<DeclarationDataReportType> ddReportTypes) {
        try {
            List<Integer> types = new ArrayList<Integer>();
            for (DeclarationDataReportType type : ddReportTypes) {
                types.add(type.getReportType().getId());
            }
            String sql = String.format("DELETE FROM DECLARATION_REPORT WHERE %s and %s",
                    SqlUtils.transformToSqlInStatement("DECLARATION_DATA_ID", declarationDataIds),
                    SqlUtils.transformToSqlInStatement("TYPE", types));
            Map<String, Object> params = new HashMap<String, Object>();
            getNamedParameterJdbcTemplate().update(sql, params);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось удалить записи", e);
        }
    }

    @Override
    public void deleteDec(String uuid) {
        try {
            getJdbcTemplate().update("DELETE FROM DECLARATION_REPORT WHERE BLOB_DATA_ID = ?",
                    new Object[]{uuid},
                    new int[]{Types.VARCHAR});
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Не удалось удалить записи с BLOB_DATA_ID = %s", uuid), e);
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
        } catch (DataAccessException e) {
            throw new DaoException(String.format("Ошибка при удалении ненужных записей таблицы DECLARATION_REPORT. %s.", e.getMessage()), e);
        }
    }

    @Override
    public void deleteNotXmlDec(long declarationDataId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("declarationDataId", declarationDataId);
            params.addValue("type", DeclarationDataReportType.XML_DEC.getReportType().getId());
            getNamedParameterJdbcTemplate().update(
                    "DELETE FROM DECLARATION_REPORT WHERE DECLARATION_DATA_ID = :declarationDataId AND TYPE <> :type", params);
        } catch (DataAccessException e) {
            throw new DaoException("Не удалось удалить записи", e);
        }
    }
}
