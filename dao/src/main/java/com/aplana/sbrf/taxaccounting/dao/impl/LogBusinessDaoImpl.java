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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

    private final class LogSystemSearchResultItemRowMapper implements RowMapper<LogSearchResultItem> {

        @Override
        public LogSearchResultItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            LogSearchResultItem log = new LogSearchResultItem();
            log.setId(rs.getLong("lb_id"));
            log.setLogDate(new Date(rs.getTimestamp("lb_log_date").getTime()));
            log.setRoles(rs.getString("lb_roles"));
            log.setFormKind(rs.getInt("form_kind_id") != 0 ? FormDataKind.fromId(rs.getInt("form_kind_id")) : null);
            log.setEvent(FormDataEvent.getByCode(rs.getInt("lb_event_id")));
            log.setUser(userDao.getUser(rs.getInt("lb_user_id")));
            /*log.setDepartment(departmentDao.getDepartment(rs.getInt("user_department_id")));*/
            log.setReportPeriod(reportPeriodDao.get(rs.getInt("report_period_id")));
            log.setDeclarationType(rs.getInt("declaration_type_id") != 0 ? declarationTypeDao.get(rs.getInt("declaration_type_id")) : null);
            log.setFormType(rs.getInt("form_type_id") != 0? formTypeDao.get(rs.getInt("form_type_id")) : null);
            log.setNote(rs.getString("lb_note"));
            log.setUserDepartment(departmentDao.getDepartment(rs.getInt("lb_user_department_id")));
            int departmentId = rs.getInt("lb_form_data_id") != 0?rs.getInt("fd_department_id") : rs.getInt("dd_department_id");
            String s = departmentDao.getParentsHierarchy(departmentId);
            log.setDepartmentName(s != null ? s : departmentDao.getDepartment(departmentId).getName());
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
    public PagingResult<LogSearchResultItem> getLogsBusiness(List<Long> formDataIds, List<Long> declarationDataIds, LogBusinessFilterValuesDao filter) {
        boolean isEventColumn = filter.getOrdering() == HistoryBusinessSearchOrdering.EVENT;

        Map<String, Object> names = new HashMap<String, Object>();
        names.put("formDataIds", formDataIds);
        names.put("declarationDataIds", declarationDataIds);
        names.put("departmentId", filter.getDepartmentId());
        names.put("userId", filter.getUserIds());
        names.put("fromDate", filter.getFromSearchDate());
        names.put("toDate", filter.getToSearchDate());
        names.put("startIndex", filter.getStartIndex() + 1);
        names.put("endIndex", filter.getStartIndex() + filter.getCountOfRecords());

        StringBuilder sql = new StringBuilder("SELECT * FROM (SELECT ");
        sql.append("fd.department_id AS fd_department_id, ");
        sql.append("dd.department_id AS dd_department_id, ");
        sql.append("fd.kind AS form_kind_id, ");
        sql.append("lb.id AS lb_id, ");
        sql.append("lb.log_date AS lb_log_date, ");
        sql.append("lb.roles AS lb_roles, ");
        sql.append("lb.event_id AS lb_event_id, ");
        sql.append("lb.user_id AS lb_user_id, ");
        sql.append("lb.note AS lb_note, ");
        sql.append("lb.user_department_id AS lb_user_department_id, ");
        sql.append("lb.form_data_id AS lb_form_data_id, ");
        if (isEventColumn) {
            sql.append("em.event_title, ");
        }
        sql.append("rp.id AS report_period_id, ");
        sql.append("dt.id AS declaration_type_id, ");
        sql.append("ft.id AS form_type_id, ");
        sql.append(orderByClause(filter.getOrdering(), filter.isAscOrdering()));
        sql.append("FROM log_business lb ");
        sql.append("LEFT JOIN form_data fd ON lb.form_data_id=fd.\"ID\" ");
        sql.append("LEFT JOIN form_template ftemp ON fd.form_template_id=ftemp.\"ID\" ");
        sql.append("LEFT JOIN form_type ft ON ftemp.type_id=ft.\"ID\" ");
        sql.append("LEFT JOIN sec_user su ON lb.user_id=su.\"ID\" ");
        sql.append("LEFT JOIN REPORT_PERIOD rp ON fd.report_period_id=rp.\"ID\" ");
        sql.append("LEFT JOIN TAX_PERIOD tp ON rp.tax_period_id=tp.\"ID\" ");
        sql.append("LEFT JOIN declaration_data dd ON lb.declaration_data_id=dd.\"ID\" ");
        sql.append("LEFT JOIN department dep ON dd.department_id=dep.\"ID\" OR fd.department_id=dep.\"ID\" ");
        sql.append("LEFT JOIN declaration_template dtemp ON dd.declaration_template_id=dtemp.\"ID\" ");
        sql.append("LEFT JOIN declaration_type dt ON dtemp.declaration_type_id=dt.\"ID\" ");
        if (isEventColumn) {
            sql.append("LEFT JOIN event_map em ON lb.event_id=em.\"EVENT_ID\" ");
        }
        sql.append("WHERE lb.log_date BETWEEN :fromDate AND :toDate + INTERVAL '1' DAY");
        sql.append(filter.getDepartmentId() == null?"":" AND fd.department_id = :departmentId or dd.department_id = :departmentId ");
        if (filter.getUserIds()!=null && !filter.getUserIds().isEmpty()){

            List<Long> userList = filter.getUserIds();
            String userSql = "";
            for(Long temp : userList){
                if (userSql.equals("")){
                    userSql = temp.toString();
                }
                else{
                    userSql = userSql + ", " + temp.toString();
                }
            }
            sql.append(" AND user_id IN ").append("(").append(userSql).append(")");
        }
        if (formDataIds != null && !formDataIds.isEmpty() && declarationDataIds != null && !declarationDataIds.isEmpty())
            sql.append(" AND (form_data_id IN (:formDataIds) OR declaration_data_id IN (:declarationDataIds))");
        else if (formDataIds != null && !formDataIds.isEmpty())
            sql.append(" AND form_data_id IN (:formDataIds)");
        else if (declarationDataIds != null && !declarationDataIds.isEmpty())
            sql.append(" AND declaration_data_id IN (:declarationDataIds)");

        sql.append(") ");
        if (filter.getCountOfRecords() != 0)
            sql.append("WHERE RN BETWEEN :startIndex and :endIndex");

        if (isEventColumn) {
            try {
                getJdbcTemplate().execute("CREATE GLOBAL TEMPORARY TABLE event_map (event_id NUMBER, event_title CHAR(100)) ON COMMIT DELETE ROWS");
            } catch (Throwable e) {
                // Выкидывает исключение если таблица существует
            }

            getJdbcTemplate().execute(insertEventTitles());
        }

        List<LogSearchResultItem> records = getNamedParameterJdbcTemplate().query(sql.toString(),
                names,
                new LogSystemSearchResultItemRowMapper()
        );

        return new PagingResult<LogSearchResultItem>(records, getCount(formDataIds, declarationDataIds, names));
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

    @Override
    public void removeRecords(final List<Long> listIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate("delete from log_business where id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, listIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return listIds.size();
                    }
                });
    }

    private int getCount(List<Long> formDataIds, List<Long> declarationDataIds,Map<String, Object> names) {
        StringBuilder sql = new StringBuilder("select count(*) from log_business where");
        sql.append(" log_date between :fromDate and :toDate + interval '1' day");
        sql.append(names.get("departmentId") == null? "" :" and user_department_id = :departmentId");
        if (names.get("userId") !=null&&!((List<Long>)names.get("userId")).isEmpty()){
            sql.append(" and user_id in (:userId)");
        }
        if (formDataIds != null && !formDataIds.isEmpty() && declarationDataIds != null && !declarationDataIds.isEmpty())
            sql.append(" and (form_data_id in (:formDataIds) or declaration_data_id in (:declarationDataIds))");
        else if (formDataIds != null && !formDataIds.isEmpty())
            sql.append(" and form_data_id in (:formDataIds)");
        else if (declarationDataIds != null && !declarationDataIds.isEmpty())
            sql.append(" and declaration_data_id in (:declarationDataIds)");
        return getNamedParameterJdbcTemplate().queryForInt(sql.toString(),
                names);
    }

    public String orderByClause(HistoryBusinessSearchOrdering ordering, boolean ascSorting) {

        StringBuilder order = new StringBuilder();

        if (isSupportOver()) {
            order.append("ROW_NUMBER () OVER (ORDER BY ");

            String column = null;

            if (ordering == null) {
                ordering = HistoryBusinessSearchOrdering.ID;
            }

            switch (ordering) {
                case ID:
                    // Сортировка по умолчанию
                    break;
                case DATE:
                    column = "lb.log_date";
                    break;
                case EVENT:
                    column = "em.event_title";
                    break;
                case NOTE:
                    column = "lb.note";
                    break;
                case REPORT_PERIOD:
                    column = "tp.year";
                    if (!ascSorting) {
                        column = column + " DESC";
                    }
                    column = column + ", rp.name";
                    break;
                case DEPARTMENT:
                    column = "dep.name";
                    break;
                case TYPE:
                    column = "CASE WHEN lb.declaration_data_id != NULL THEN lb.declaration_data_id ELSE lb.form_data_id END";
                    break;
                case FORM_DATA_KIND:
                    column = "fd.kind";
                    break;
                case FORM_TYPE:
                    column = "ftemp.name";
                    break;
                case USER:
                    column = "su.name";
                    break;
                case USER_ROLE:
                    column = "lb.roles";
                    break;
                case IP_ADDRESS:
                    column = "";//TODO отсутствует в history_business
                    break;
            }

            if (column != null) {
                order.append(column);
                if (!ascSorting) {
                    order.append(" DESC");
                }
                order.append(", ");
            }

            // Сортировка по умолчанию
            order.append("lb.id");
            if (!ascSorting) {
                order.append(" DESC");
            }
            order.append(") RN ");
        } else {
            order.append("ROW_NUMBER () OVER () RN ");
        }

        return order.toString();
    }

    private String insertEventTitles() {
        StringBuilder query = new StringBuilder();
        FormDataEvent values[] = FormDataEvent.values();

        query.append("INSERT ALL ");

        for (FormDataEvent value : values) {
            query.append("INTO event_map(event_id, event_title) VALUES (" + value.getCode() + ", '" + value.getTitle() + "') ");
        }
        query.append("SELECT * FROM DUAL ");

        return query.toString();
    }
}
