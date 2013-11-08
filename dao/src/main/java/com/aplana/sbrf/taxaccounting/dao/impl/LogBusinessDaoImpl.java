package com.aplana.sbrf.taxaccounting.dao.impl;


import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.LogBusinessDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;


@Repository
public class LogBusinessDaoImpl extends AbstractDao implements LogBusinessDao {

    @Autowired
    private FormTypeDao formTypeDao;
    @Autowired
    private DeclarationTypeDao declarationTypeDao;
    @Autowired
    private TAUserDao userDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private ReportPeriodDao reportPeriodDao;

	private static final String DECLARATION_NOT_FOUND_MESSAGE = "Декларация с id = %d не найдена в БД";
	private static final String FORM_NOT_FOUND_MESSAGE = "Налоговая форма с id = %d не найдена в БД";

	private static final class LogBusinessRowMapper implements RowMapper<LogBusiness> {
		@Override
		public LogBusiness mapRow(ResultSet rs, int index) throws SQLException {
			LogBusiness log = new LogBusiness();
			log.setId(rs.getLong("id"));
			log.setLogDate(new Date(rs.getTimestamp("log_date").getTime()));
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

    private final class LogBusinessSearchResultItemRowMapper implements RowMapper<LogBusinessSearchResultItem> {

        @Override
        public LogBusinessSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            LogBusinessSearchResultItem log = new LogBusinessSearchResultItem();
            log.setId(rs.getLong("id"));
            log.setLogDate(new Date(rs.getTimestamp("log_date").getTime()));
            log.setRoles(rs.getString("roles"));
            log.setFormKind(rs.getInt("form_kind_id") != 0 ? FormDataKind.fromId(rs.getInt("form_kind_id")) :null);
            log.setEvent(FormDataEvent.getByCode(rs.getInt("event_id")));
            log.setUser(userDao.getUser(rs.getInt("user_id")));
            log.setRoles(rs.getString("roles"));
            log.setDepartment(departmentDao.getDepartment(rs.getInt("user_department_id")));
            log.setReportPeriod(reportPeriodDao.get(rs.getInt("report_period_id")));
            log.setDeclarationType(rs.getInt("declaration_type_id") != 0 ? declarationTypeDao.get(rs.getInt("declaration_type_id")) : null);
            log.setFormType(rs.getInt("form_type_id") != 0? formTypeDao.get(rs.getInt("form_type_id")) : null);
            log.setNote(rs.getString("note"));
            log.setUserDepartment(departmentDao.getDepartment(rs.getInt("user_department_id")));
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
    public PagingResult<LogBusinessSearchResultItem> getLogsBusiness(List<Long> formDataIds, List<Long> declarationDataIds, LogBusinessFilterValuesDao filter) {
        Map<String, Object> names = new HashMap<String, Object>();
        names.put("formDataIds", formDataIds);
        names.put("declarationDataIds", declarationDataIds);
        names.put("departmentId", filter.getDepartmentId());
        names.put("fromDate", filter.getFromSearchDate());
        names.put("toDate", filter.getToSearchDate());
        names.put("startIndex", filter.getStartIndex() + 1);
        names.put("endIndex", filter.getStartIndex() + filter.getCountOfRecords());

        StringBuilder sql = new StringBuilder("select * from (select fd.kind as form_kind_id, lb.*, rp.id as report_period_id, dt.id as declaration_type_id, ft.id as form_type_id, " +
                "rownum as rn from log_business lb ");
        sql.append("left join form_data fd on lb.form_data_id=fd.\"ID\" ");
        sql.append("left join form_template ftemp on fd.form_template_id=ftemp.\"ID\" ");
        sql.append("left join form_type ft on ftemp.type_id=ft.\"ID\" ");
        sql.append("left join department dep on lb.user_department_id=dep.\"ID\" ");
        sql.append("left join sec_user su on lb.user_id=su.\"ID\" ");
        sql.append("left join REPORT_PERIOD rp on fd.report_period_id=rp.\"ID\" ");
        sql.append("left join TAX_PERIOD tp on rp.tax_period_id=tp.\"ID\" ");
        sql.append("left join declaration_data dd on lb.declaration_data_id=dd.\"ID\" ");
        sql.append("left join declaration_template dtemp on dd.declaration_template_id=dtemp.\"ID\" ");
        sql.append("left join declaration_type dt on dtemp.declaration_type_id=dt.\"ID\" ");
        sql.append(" where form_data_id in (:formDataIds) ").
                append("or declaration_data_id in (:declarationDataIds) and user_department_id = :departmentId and log_date between :fromDate and :toDate)");
        sql.append(" where rn between :startIndex and :endIndex order by id desc");
        List<LogBusinessSearchResultItem> records = getNamedParameterJdbcTemplate().query(sql.toString(),
                names,
                new LogBusinessSearchResultItemRowMapper()
        );

        return new PagingResult<LogBusinessSearchResultItem>(records, getCount(filter, names));
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

    private int getCount(LogBusinessFilterValuesDao filter, Map<String, Object> names) {
        return getNamedParameterJdbcTemplate().queryForInt("select count(*) from log_business where form_data_id in (:formDataIds) " +
                "or declaration_data_id in (:declarationDataIds) and user_department_id = :departmentId and log_date between :fromDate and :toDate",
                names);
    }
}
