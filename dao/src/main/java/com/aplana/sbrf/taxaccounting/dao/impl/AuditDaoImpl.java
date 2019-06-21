package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {

    private static final Log LOG = LogFactory.getLog(AuditDaoImpl.class);

    @Override
    public void add(LogSystem logSystem) {
        try {
            JdbcTemplate jt = getJdbcTemplate();

            SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(jt).withProcedureName("Add_Log_System_NDFL").withoutProcedureColumnMetaDataAccess().declareParameters(
                    new SqlParameter("p_ip", Types.VARCHAR),
                    new SqlParameter("p_event_id", Types.NUMERIC),
                    new SqlParameter("p_user_login", Types.VARCHAR),
                    new SqlParameter("p_roles", Types.VARCHAR),
                    new SqlParameter("p_department_name", Types.VARCHAR),
                    new SqlParameter("p_report_period_name", Types.VARCHAR),
                    new SqlParameter("p_form_kind_id", Types.NUMERIC),
                    new SqlParameter("p_note", Types.VARCHAR),
                    new SqlParameter("p_user_department_name", Types.VARCHAR),
                    new SqlParameter("p_declaration_type_name", Types.VARCHAR),
                    new SqlParameter("p_form_type_name", Types.VARCHAR),
                    new SqlParameter("p_form_department_id", Types.NUMERIC),
                    new SqlParameter("p_form_type_id", Types.NUMERIC),
                    new SqlParameter("p_is_error", Types.NUMERIC),
                    new SqlParameter("p_audit_form_type_id", Types.NUMERIC),
                    new SqlParameter("p_server", Types.VARCHAR),
                    new SqlParameter("p_user_id", Types.NUMERIC),
                    new SqlParameter("p_blob_data_id", Types.VARCHAR)
            );
            simpleJdbcCall.compile();

            SqlParameterSource in = new MapSqlParameterSource()
                    .addValue("p_event_id", logSystem.getEventId())
                    .addValue("p_note", logSystem.getNote())
                    .addValue("p_user_id", null)
                    .addValue("p_user_login", logSystem.getUserLogin())
                    .addValue("p_ip", logSystem.getIp())
                    .addValue("p_roles", null)
                    .addValue("p_user_department_name", null)
                    .addValue("p_is_error", logSystem.getIsError())
                    .addValue("p_server", logSystem.getServer())
                    .addValue("p_report_period_name", logSystem.getReportPeriodName())
                    .addValue("p_department_name", logSystem.getFormDepartmentName())
                    .addValue("p_form_department_id", logSystem.getFormDepartmentId())
                    .addValue("p_declaration_type_name", null)
                    .addValue("p_form_kind_id", null)
                    .addValue("p_form_type_name", logSystem.getFormTypeName())
                    .addValue("p_form_type_id", null)
                    .addValue("p_audit_form_type_id", logSystem.getAuditFormTypeId())
                    .addValue("p_blob_data_id", null);
            simpleJdbcCall.execute(in);
        } catch (DataAccessException e) {
            LOG.error("Ошибки при логировании.", e);
            throw new DaoException("Ошибки при логировании.", e);
        }
    }
}