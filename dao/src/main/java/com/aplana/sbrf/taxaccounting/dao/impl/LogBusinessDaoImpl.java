package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
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
			log.setId(SqlUtils.getLong(rs, "id"));
			log.setLogDate(new Date(rs.getTimestamp("log_date").getTime()));
			log.setEventId(SqlUtils.getInteger(rs,"event_id"));
			log.setUserLogin(rs.getString("user_login"));
			log.setRoles(rs.getString("roles"));
			log.setDeclarationId(SqlUtils.getLong(rs, "declaration_data_id"));
			log.setFormId(SqlUtils.getLong(rs, "form_data_id"));
			log.setDepartmentId(SqlUtils.getInteger(rs,"user_department_id"));
			log.setNote(rs.getString("note"));
			return log;
		}
	}

	@Override
	public List<LogBusiness> getDeclarationLogsBusiness(long declarationId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder("select log_business.* from log_business\n");
        if (ordering == HistoryBusinessSearchOrdering.EVENT)
            sql.append("left join event ev on log_business.event_id=ev.\"ID\"\n");
        sql.append("where declaration_data_id = ?\n");
        sql.append(sortingClause(ordering, isAscSorting));
        try {
			return getJdbcTemplate().query(
					sql.toString(),
					new Object[]{declarationId},
					new LogBusinessRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			throw new DaoException(DECLARATION_NOT_FOUND_MESSAGE, declarationId);
		}
	}

	@Override
	public List<LogBusiness> getFormLogsBusiness(long formId, HistoryBusinessSearchOrdering ordering, boolean isAscSorting) {
        StringBuilder sql = new StringBuilder("select log_business.* from log_business\n");
        if (ordering == HistoryBusinessSearchOrdering.EVENT)
            sql.append("left join event ev on log_business.event_id=ev.\"ID\"\n");
        sql.append("where form_data_id = ?\n");
        sql.append(sortingClause(ordering, isAscSorting));
        try {
			return getJdbcTemplate().query(
					sql.toString(),
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
					" (event_id = ? or event_id = ? or event_id = ? or event_id = ?)",
					new Object[]{formId, FormDataEvent.MOVE_APPROVED_TO_ACCEPTED.getCode(),
							FormDataEvent.MOVE_CREATED_TO_ACCEPTED.getCode(),
							FormDataEvent.MOVE_PREPARED_TO_ACCEPTED.getCode(),
                            FormDataEvent.MOVE_PREPARED_TO_APPROVED.getCode()}, Timestamp.class
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
				"insert into log_business (id, log_date, event_id, user_login, roles, declaration_data_id, form_data_id, user_department_id, note)" +
						" values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
				id,
				logBusiness.getLogDate(),
				logBusiness.getEventId(),
				logBusiness.getUserLogin(),
				logBusiness.getRoles(),
				logBusiness.getDeclarationId(),
				logBusiness.getFormId(),
				logBusiness.getDepartmentId(),
				logBusiness.getNote()
		);
	}

    private String sortingClause(HistoryBusinessSearchOrdering ordering, boolean isAscSorting) {

        StringBuilder clause = new StringBuilder();

        switch (ordering) {
            case EVENT:
                clause.append("ORDER BY ev.name");
                break;
            case DATE:
                clause.append("ORDER BY log_date");
                break;
            case USER:
                clause.append("ORDER BY user_login");
                break;
            case USER_ROLE:
                clause.append("ORDER BY roles");
                break;
            case DEPARTMENT:
                clause.append("ORDER BY user_department_id");
                break;
            case NOTE:
                clause.append("ORDER BY note");
                break;
        }

        if (!isAscSorting) clause.append(" DESC");

        return clause.toString();
    }
}
