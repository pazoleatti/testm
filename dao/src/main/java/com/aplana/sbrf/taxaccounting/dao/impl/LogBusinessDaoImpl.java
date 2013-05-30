package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.LogBusiness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Repository
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

			if (rs.getLong("declaration_data_id") != 0) {
				log.setDeclarationId(rs.getLong("declaration_data_id"));
			} else {
				log.setDeclarationId(null);
			}

			if (rs.getLong("form_data_id") != 0) {
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
	public Date getFormAcceptanceDate(long formId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select max(log_date) from log_business where form_data_id = ? and" +
					" (event_id = ? or event_id = ? or event_id = ?)",
					new Object[]{formId, FormDataEvent.MOVE_APPROVED_TO_ACCEPTED.getCode(),
							FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getCode(),
							FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getCode()}, Timestamp.class
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public Date getFormCreationDate(long formId) {
		try {
			return getJdbcTemplate().queryForObject(
					"select log_date from log_business where form_data_id = ? and event_id = ? ",
					new Object[]{formId, FormDataEvent.CREATE.getCode()}, Timestamp.class
			);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
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
