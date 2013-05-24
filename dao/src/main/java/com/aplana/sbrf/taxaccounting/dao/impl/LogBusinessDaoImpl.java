package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@Repository("declarationLogBusinessDao")
@Transactional(readOnly = true)
public class LogBusinessDaoImpl extends AbstractDao implements LogBusinessDao {

	private static final String DECLARATION_NOT_FOUND_MESSAGE = "Декларация с id = %d не найдена в БД";
	private static final String FORM_NOT_FOUND_MESSAGE = "Налоговая форма с id = %d не найдена в БД";

	private static final class LogBusinessRowMapper implements RowMapper<LogBusiness> {
		@Override
		public LogBusiness mapRow(ResultSet rs, int index) throws SQLException {
			LogBusiness log = new LogBusiness();
			log.setId(rs.getLong("id"));
			log.setLogDate(rs.getTimestamp("log_date"));
			log.setEventId(rs.getInt("event_id"));
			log.setUserId(rs.getInt("user_id"));
			log.setRoles(rs.getString("roles"));

			if (rs.getInt("declaration_data_id") != 0) {
				log.setDeclarationId(rs.getLong("declaration_data_id"));
			} else {
				log.setDeclarationId(null);
			}

			if (rs.getInt("form_data_id") != 0) {
				log.setFormId(rs.getLong("form_data_id"));
			} else {
				log.setFormId(null);
			}

			log.setDepartmentId(rs.getInt("user_department_id"));
			log.setNote(rs.getString("note"));
			return log;
		}
	}

	@Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId) {
		try {
			return getJdbcTemplate().query(
					"select * from log_business where declaration_data_id = ? order by log_date desc",
					new Object[]{declarationId},
					new LogBusinessRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(DECLARATION_NOT_FOUND_MESSAGE, declarationId);
		}
	}

	@Override
	public List<LogBusiness> getFormLogsBusiness(long formId) {
		try {
			return getJdbcTemplate().query(
					"select * from log_business where form_data_id = ? order by log_date desc",
					new Object[]{formId},
					new LogBusinessRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(FORM_NOT_FOUND_MESSAGE, formId);
		}
	}

	@Override
	@Transactional(readOnly = false)
	public void add(LogBusiness logBusiness) {
		JdbcTemplate jt = getJdbcTemplate();

		Long id = logBusiness.getId();
		if (id == null) {
			id = generateId("seq_log_business", Long.class);
		}

		jt.update(
				"insert into log_business (id, log_date, event_id, user_id, roles, declaration_data_id, form_data_id, user_department_id, note)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				logBusiness.getLogDate(),
				logBusiness.getEventId(),
				logBusiness.getUserId(),
				logBusiness.getRoles(),
				logBusiness.getDeclarationId(),
				logBusiness.getFormId(),
				logBusiness.getDepartmentId(),
				logBusiness.getNote()
		);
	}
}
