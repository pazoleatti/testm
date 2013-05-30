package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.LogSystem;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {
	private static final String dbDateFormat = "YYYYMMDD HH:MM:SS";
	private static final SimpleDateFormat formatter = new SimpleDateFormat(dbDateFormat);

	@Override
	public List<LogSystem> getLogs(LogSystemFilter filter) {
		StringBuilder sql = new StringBuilder();
		appendSelectFromAndWhereClause(sql, filter);
		sql.append(" order by id");
		return getJdbcTemplate().query(sql.toString(), new AuditRowMapper());
	}

	@Override
	public void add(LogSystem logSystem) {
		JdbcTemplate jt = getJdbcTemplate();

		Long id = logSystem.getId();
		if (id == null) {
			id = generateId("seq_log_system", Long.class);
		}

		jt.update(
				"insert into log_system (id, log_date, ip, event_id, user_id, roles, department_id, report_period_id, " +
						"declaration_type_id, form_type_id, form_kind_id, note, user_department_id)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				logSystem.getLogDate(),
				logSystem.getIp(),
				logSystem.getEventId(),
				logSystem.getUserId(),
				logSystem.getRoles(),
				logSystem.getDepartmentId(),
				logSystem.getReportPeriodId(),
				logSystem.getDeclarationTypeId(),
				logSystem.getFormTypeId(),
				logSystem.getFormKindId(),
				logSystem.getNote(),
				logSystem.getUserDepartmentId()
		);
	}

	private void appendSelectFromAndWhereClause(StringBuilder sql, LogSystemFilter filter) {
		sql.append("SELECT * FROM log_system WHERE log_date BETWEEN TO_DATE('").append(formatter.format(filter.getFromSearchDate()))
				.append("', '").append(dbDateFormat).append("')").append(" AND TO_DATE('")
				.append(formatter.format(filter.getToSearchDate())).append("', '").append(dbDateFormat).append("')");

		if (filter.getUserId() != 0) {
			sql.append(" AND user_id = ").append(filter.getUserId());
		}

		if (filter.getReportPeriodId() != 0) {
			sql.append(" AND report_period_id = ").append(filter.getReportPeriodId());
		}

		if (filter.getFormKindId() != 0) {
			sql.append(" AND form_kind_id = ").append(filter.getFormKindId());
		}

		if (filter.getFormTypeId() != 0) {
			sql.append(" AND form_type_id = ").append(filter.getFormTypeId());
		}

		if (filter.getDeclarationTypeId() != 0) {
			sql.append(" AND declaration_type_id ").append(filter.getDeclarationTypeId());
		}

		if (filter.getDepartmentId() != 0) {
			sql.append(" AND department_id ").append(filter.getDepartmentId());
		}
	}

	private static final class AuditRowMapper implements RowMapper<LogSystem> {
		@Override
		public LogSystem mapRow(ResultSet rs, int index) throws SQLException {
			LogSystem log = new LogSystem();
			log.setId(rs.getLong("id"));
			log.setLogDate(rs.getDate("log_date"));
			log.setIp(rs.getString("ip"));
			log.setEventId(rs.getInt("event_id"));
			log.setUserId(rs.getInt("user_id"));
			log.setRoles(rs.getString("roles"));
			log.setDepartmentId(rs.getInt("user_department_id"));
			log.setReportPeriodId(rs.getInt("report_period_id"));
			log.setDeclarationTypeId(rs.getInt("declaration_type_id"));
			log.setFormTypeId(rs.getInt("form_type_id"));
			log.setFormKindId(rs.getInt("form_kind_id"));
			log.setNote(rs.getString("note"));
			log.setUserDepartmentId(rs.getInt("user_department_id"));
			return log;
		}
	}
}
