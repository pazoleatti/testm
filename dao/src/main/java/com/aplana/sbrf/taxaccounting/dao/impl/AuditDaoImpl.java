package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {
	private static final String dbDateFormat = "YYYYMMDD HH:MM:SS";
	private static final String dateFormat = "yyyyMMdd HH:MM:SS";
	private static final SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
	private static final long oneDayTime = 1000 * 60 * 60 * 24;

	@Override
	public List<LogSystemSearchResultItem> getLogs(LogSystemFilter filter) {
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
		sql.append("SELECT * FROM log_system WHERE log_date BETWEEN TO_DATE('").append
				(formatter.format(new Date(filter.getFromSearchDate().getTime() - oneDayTime)))
				.append("', '").append(dbDateFormat).append("')").append(" AND TO_DATE('").append
				(formatter.format(new Date(filter.getToSearchDate().getTime() + oneDayTime)))
				.append("', '").append(dbDateFormat).append("')");

		if (filter.getUserId() != 0) {
			sql.append(" AND user_id = ").append(filter.getUserId());
		}

		if (filter.getReportPeriodIds().size() != 0) {
			sql.append(" AND report_period_id = ").append(filter.getReportPeriodIds().get(0));
		}

		if (filter.getFormKind() != null && filter.getFormKind().getId() !=0) {
			sql.append(" AND form_kind_id = ").append(filter.getFormKind().getId());
		}

		if (filter.getFormTypeId() != 0) {
			sql.append(" AND form_type_id = ").append(filter.getFormTypeId());
		}

		if (filter.getDeclarationTypeId() != 0) {
			sql.append(" AND declaration_type_id ").append(filter.getDeclarationTypeId());
		}

		if (filter.getDepartmentIds().size() != 0) {
			sql.append(" AND department_id ").append(filter.getDepartmentIds().get(0));
		}
	}

	private static final class AuditRowMapper implements RowMapper<LogSystemSearchResultItem> {
		@Override
		public LogSystemSearchResultItem mapRow(ResultSet rs, int index) throws SQLException {
			LogSystemSearchResultItem log = new LogSystemSearchResultItem();
			log.setId(rs.getLong("id"));
			log.setLogDate(rs.getDate("log_date"));
			log.setIp(rs.getString("ip"));
			log.setEvent(FormDataEvent.getByCode(rs.getInt("event_id")));
			//log.setUserId(rs.getInt("user_id"));
			log.setRoles(rs.getString("roles"));
			//log.setDepartmentId(rs.getInt("user_department_id"));
			//log.setReportPeriod(rs.getInt("report_period_id"));
			//log.setDeclarationTypeId(rs.getInt("declaration_type_id"));
			//log.setFormTypeId(rs.getInt("form_type_id"));
			log.setFormKind(FormDataKind.fromId(rs.getInt("form_kind_id")));
			log.setNote(rs.getString("note"));
			//log.setUserDepartmentId(rs.getInt("user_department_id"));

			return log;
		}
	}
}
