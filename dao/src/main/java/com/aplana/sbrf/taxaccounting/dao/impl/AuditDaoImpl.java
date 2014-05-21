package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.TAUserDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.*;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private DepartmentDao departmentDao;
    @Autowired
	private DeclarationTypeDao declarationTypeDao;
	@Autowired
	private FormTypeDao formTypeDao;

	@Override
	public PagingResult<LogSearchResultItem> getLogs(LogSystemFilter filter) {
        boolean isEventColumn = filter.getSearchOrdering() == HistoryBusinessSearchOrdering.EVENT;

        if (isEventColumn) {
            try {
                getJdbcTemplate().execute("CREATE GLOBAL TEMPORARY TABLE event_map (event_id NUMBER, event_title CHAR(100)) ON COMMIT DELETE ROWS");
            } catch (Throwable e) {
                // Выкидывает исключение если таблица существует
            }

            getJdbcTemplate().execute(insertEventTitles());
        }

		StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from ( select ");
        sql.append("ls.id, ");
        sql.append("ls.log_date, ");
        sql.append("ls.ip, ");
        sql.append("ls.event_id, ");
        sql.append("ls.user_id, ");
        sql.append("ls.roles, ");
        sql.append("ls.department_id, ");
        sql.append("ls.report_period_name, ");
        sql.append("ls.declaration_type_id, ");
        sql.append("ls.form_type_id, ");
        sql.append("ls.form_kind_id, ");
        sql.append("ls.note, ");
        if (isEventColumn) {
            sql.append("em.event_title, ");
        }
        sql.append("ls.user_department_id ");
        sql.append(" from log_system ls ");

        sql.append("left join department dep on ls.department_id=dep.\"ID\" ");
        sql.append("left join form_type ft on ls.form_type_id=ft.\"ID\" ");
        sql.append("left join declaration_type dt on ls.declaration_type_id=dt.\"ID\" ");
        sql.append("left join sec_user su on ls.user_id=su.\"ID\" ");
        if (isEventColumn) {
            sql.append("LEFT JOIN event_map em ON ls.event_id=em.\"EVENT_ID\" ");
        }

		appendSelectWhereClause(sql, filter, "ls.");

        sql.append(orderByClause(filter.getSearchOrdering(), filter.isAscSorting()));

		sql.append(") dat) ordDat");
        List<LogSearchResultItem> records;
        if(filter.getCountOfRecords() != 0){
            sql.append(" where ordDat.rn between ? and ?")
                    .append(" order by ordDat.rn");
            records = getJdbcTemplate().query(
                    sql.toString(),
                    new Object[] {
		                    filter.getFromSearchDate(),
		                    filter.getFromSearchDate(),
		                    filter.getToSearchDate(),
                            filter.getStartIndex() + 1,	// В java нумерация с 0, в БД row_number() нумерует с 1
                            filter.getStartIndex() + filter.getCountOfRecords()
                    },
                    new int[] {
		                    Types.DATE,
		                    Types.DATE,
		                    Types.DATE,
                            Types.NUMERIC,
                            Types.NUMERIC
                    },
                    new AuditRowMapper()
            );
        }else{
            sql.append(" order by ordDat.rn");
            records = getJdbcTemplate().query(sql.toString(),
		            new Object[] {
			            filter.getFromSearchDate(),
			            filter.getFromSearchDate(),
			            filter.getToSearchDate()},
		            new int[] {
				            Types.DATE,
				            Types.DATE,
				            Types.DATE,
		            },
                    new AuditRowMapper());
        }

		return new PagingResult<LogSearchResultItem>(records, getCount(filter));
	}

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilterDao filter) {

        Map<String, Object> names = new HashMap<String, Object>();
        names.put("fromDate", filter.getFromSearchDate());
        names.put("endDate", filter.getToSearchDate());
        names.put("number", filter.getCountOfRecords());
        names.put("rpName", "%" + filter.getReportPeriodName() + "%");
        names.put("startIdx", filter.getStartIndex() + 1);
        names.put("endIdx", filter.getStartIndex() + filter.getCountOfRecords());
        names.put("userIds", filter.getUserIds());

        StringBuilder sql = new StringBuilder();
        appendWithClause(sql, filter);
        sql.append("select ordDat.* from ( ");
        sql.append("select dat.*, rownum as rn from (");
        sql.append("select ls.id, ls.log_date, ls.ip, ls.event_id, ls.user_id, ls.roles, ls.department_id, ls.declaration_type_id, ls.form_type_id, ls.form_kind_id, ls.note, ls.user_department_id, ls.report_period_name ");
        sql.append("FROM log_system ls ");
        appendJoinWhereClause(sql, filter);
        sql.append(orderByClause(filter.getSearchOrdering(), filter.isAscSorting()));
        sql.append(") dat) ordDat");
        if(filter.getCountOfRecords() != 0){
            sql.append(" where ordDat.rn between :startIdx and :endIdx");
        }

        try {
            List<LogSearchResultItem> records = getNamedParameterJdbcTemplate().query(sql.toString(),
                    names,
                    new AuditRowMapper()
            );

            return new PagingResult<LogSearchResultItem>(records, getCount(filter, names));
        } catch (DataAccessException e){
            logger.error("Ошибки при поиске записей по НФ/декларациям.", e);
            throw new DaoException("Ошибки при поиске записей по НФ/декларациям.", e);
        }

    }

	@Override
	public void add(LogSystem logSystem) {
        try {
            JdbcTemplate jt = getJdbcTemplate();

            Long id = logSystem.getId();
            if (id == null) {
                id = generateId("seq_log_system", Long.class);
            }

            jt.update(
                    "insert into log_system (id, log_date, ip, event_id, user_id, roles, department_id, report_period_name, " +
                            "declaration_type_id, form_type_id, form_kind_id, note, user_department_id)" +
                            " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id,
                    logSystem.getLogDate(),
                    logSystem.getIp(),
                    logSystem.getEventId(),
                    logSystem.getUserId(),
                    logSystem.getRoles(),
                    logSystem.getDepartmentId(),
                    logSystem.getReportPeriodName(),
                    logSystem.getDeclarationTypeId(),
                    logSystem.getFormTypeId(),
                    logSystem.getFormKindId(),
                    logSystem.getNote(),
                    logSystem.getUserDepartmentId()
            );
        } catch (DataAccessException e){
            logger.error("Ошибки при логировании.", e);
            throw new DaoException("Ошибки при логировании.", e);
        }
	}

    @Override
    public void removeRecords(final List<Long> listIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.batchUpdate("delete from log_system where id = ?",
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

    @Override
    public Date lastArchiveDate() {
        try {
            return getJdbcTemplate().queryForObject("select log_date from log_system where event_id = 601", Date.class);
        } catch (EmptyResultDataAccessException e){
            logger.warn("Не найдено записей об архивации.", e);
            return null;
        } catch (IncorrectResultSizeDataAccessException e){
            logger.error("Найдено больше одной записи об архивировании.", e);
            throw new DaoException("Найдено больше одной записи об архивировании.", e);
        } catch (DataAccessException e){
            logger.error("Ошибка при получении даты последней архивации", e);
            throw new DaoException("Ошибка при получении даты последней архивации", e);
        }

    }

    private void appendSelectWhereClause(StringBuilder sql, LogSystemFilter filter, String prefix) {
		sql.append(" WHERE (? is null or (log_date BETWEEN ? AND (? + interval '1' day)))");

		if (filter.getUserIds()!=null && !filter.getUserIds().isEmpty()) {
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
            sql.append(String.format(" AND %suser_id in ", prefix)).append("(").append(userSql).append(")");
		}

		if (filter.getReportPeriodName() != null) {
            sql.append(String.format(" AND %sreport_period_name LIKE \'", prefix))
                    .append("%").append(filter.getReportPeriodName()).append("%\'");
		}

		if (filter.getFormKind() != null && filter.getFormKind().getId() != 0) {
			sql.append(String.format(" AND %sform_kind_id = ", prefix)).append(filter.getFormKind().getId());
		}

		if (filter.getFormTypeId() != null) {
			sql.append(String.format(" AND %sform_type_id = ", prefix)).append(filter.getFormTypeId());
		}

		if (filter.getDeclarationTypeId() != null) {
			sql.append(String.format(" AND %sdeclaration_type_id = ", prefix)).append(filter.getDeclarationTypeId());
		}

        if (filter.getAuditFormTypeId() != null) {
            if (filter.getAuditFormTypeId().equals(AuditFormType.FORM_TYPE_TAX.getId())) {
                sql.append(String.format(" AND %sform_type_id is not null ", prefix));
            } else if (filter.getAuditFormTypeId().equals(AuditFormType.FORM_TYPE_DECLARATION.getId())) {
                sql.append(String.format(" AND %sdeclaration_type_id is not null ", prefix));
            }
        }

        if (filter.getTaxType() != null){
            List<String> rpNames = expressionForReportNames(filter.getTaxType());
            sql.append(String.format(" AND %sreport_period_name IN (", prefix));
            for (String rpName : rpNames) {
                sql.append("\'").append(rpName).append("\'").append(",");
            }
            sql.replace(sql.length() - 1, sql.length(), "");
            sql.append(")");
        }

		if (filter.getDepartmentId() != null) {
            sql.append(String.format(" AND %sdepartment_id = ", prefix)).append(filter.getDepartmentId());
		}
	}

	private final class AuditRowMapper implements RowMapper<LogSearchResultItem> {
		@Override
		public LogSearchResultItem mapRow(ResultSet rs, int index) throws SQLException {
			LogSearchResultItem log = new LogSearchResultItem();
			log.setId(rs.getLong("id"));
			log.setLogDate(new Date(rs.getTimestamp("log_date").getTime()));
			log.setIp(rs.getString("ip"));
			log.setEvent(FormDataEvent.getByCode(rs.getInt("event_id")));
			log.setUser(userDao.getUser(rs.getInt("user_id")));
			log.setRoles(rs.getString("roles"));
			log.setDepartment(departmentDao.getDepartment(rs.getInt("department_id")));
            log.setDepartmentName(rs.getInt("department_id")!=0?departmentDao.getParentsHierarchy(rs.getInt("department_id")):log.getDepartment().getName());
			log.setReportPeriodName(rs.getString("report_period_name"));
            if(rs.getInt("declaration_type_id") != 0)
			    log.setDeclarationType(declarationTypeDao.get(rs.getInt("declaration_type_id")));
            if(rs.getInt("form_type_id") != 0)
			    log.setFormType(formTypeDao.get(rs.getInt("form_type_id")));
			if (rs.getInt("form_kind_id") != 0) {
				log.setFormKind(FormDataKind.fromId(rs.getInt("form_kind_id")));
			}
			log.setNote(rs.getString("note"));
			log.setUserDepartment(departmentDao.getDepartment(rs.getInt("user_department_id")));
			return log;
		}
	}

	private int getCount(LogSystemFilter filter) {
		StringBuilder sql = new StringBuilder("select count(*) from log_system ls ");
		appendSelectWhereClause(sql, filter, "");
		return getJdbcTemplate().queryForInt(
				sql.toString(),
				new Object[] {
					filter.getFromSearchDate(),
					filter.getFromSearchDate(),
					filter.getToSearchDate()
				},
				new int[] {
						Types.DATE,
						Types.DATE,
						Types.DATE,
				}
		);
	}

    private int getCount(LogSystemFilterDao filter, Map<String, Object> names) {
        StringBuilder sql = new StringBuilder();
        appendWithClause(sql, filter);

        sql.append(" select COUNT(*) from log_system ls");
        appendJoinWhereClause(sql, filter);
        return getNamedParameterJdbcTemplate().queryForInt(sql.toString(), names);
    }

    public String orderByClause(HistoryBusinessSearchOrdering ordering, boolean ascSorting) {

        StringBuilder order = new StringBuilder();

        order.append(" order by ");

        String column = null;

        if (ordering == null) {
            ordering = HistoryBusinessSearchOrdering.DATE;
        }

        switch (ordering) {
            case ID:
                // Сортировка по умолчанию
                break;
            case DATE:
                column = "ls.log_date";
                break;
            case EVENT:
                column = "em.event_title";
                break;
            case NOTE:
                column = "ls.note";
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
                column = "CASE WHEN ls.declaration_type_id != NULL THEN ls.declaration_type_id ELSE ls.form_type_id END";
                break;
            case FORM_DATA_KIND:
                column = "ls.form_kind_id";
                break;
            case FORM_TYPE:
                column = "ft.name";
                break;
            case USER:
                column = "su.name";
                break;
            case USER_ROLE:
                column = "ls.roles";
                break;
            case IP_ADDRESS:
                column = "ls.ip";
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
        order.append("ls.id");
        if (!ascSorting) {
            order.append(" DESC");
        }

        return order.toString();
    }

    private String insertEventTitles() {
        StringBuilder query = new StringBuilder();
        FormDataEvent values[] = FormDataEvent.values();

        query.append("INSERT ALL ");

        for (FormDataEvent value : values) {
            query.append("INTO event_map(event_id, event_title) VALUES (").append(value.getCode()).append(", '").append(value.getTitle()).append("') ");
        }
        query.append("SELECT * FROM DUAL ");

        return query.toString();
    }

    private void appendWithClause(StringBuilder sql, LogSystemFilterDao filterDao){
        sql.append("WITH");
        switch (filterDao.getAuditFormTypeId() != null ? filterDao.getAuditFormTypeId() : 0){
            case 1:
                sql.append(" fdSelection as (SELECT department_id, ftype.id AS form_type_id, kind, report_period_id FROM form_data fd ");
                sql.append(" LEFT JOIN form_template ft ON ft.id = fd.form_template_id ");
                sql.append(" LEFT JOIN form_type ftype ON ftype.id = ft.type_id ");
                sql.append(" WHERE fd.id in (").append(appendFromAndWhereClauseFD(filterDao)).append("))");
                break;
            case 2:
                sql.append(" ddSelection as(SELECT department_id, dtype.id AS declaration_type_id, report_period_id FROM declaration_data dd");
                sql.append(" LEFT JOIN declaration_template dt ON dt.id = dd.declaration_template_id");
                sql.append(" LEFT JOIN declaration_type dtype ON dtype.id = dt.declaration_type_id");
                sql.append(" WHERE dd.id in (").append(appendFromAndWhereClauseDD(filterDao)).append("))");
                break;
            default:
                sql.append(" fdSelection as (SELECT department_id, ftype.id AS form_type_id, kind, report_period_id FROM form_data fd ");
                sql.append(" LEFT JOIN form_template ft ON ft.id = fd.form_template_id ");
                sql.append(" LEFT JOIN form_type ftype ON ftype.id = ft.type_id ");
                sql.append(" WHERE fd.id in (").append(appendFromAndWhereClauseFD(filterDao)).append(")),");

                sql.append(" ddSelection as(SELECT department_id, dtype.id AS declaration_type_id, report_period_id FROM declaration_data dd");
                sql.append(" LEFT JOIN declaration_template dt ON dt.id = dd.declaration_template_id");
                sql.append(" LEFT JOIN declaration_type dtype ON dtype.id = dt.declaration_type_id");
                sql.append(" WHERE dd.id in (").append(appendFromAndWhereClauseDD(filterDao)).append("))");
        }
    }

    //Формирует запрос для поиска связей для корректого вывода имен
    private void appendJoinWhereClause(StringBuilder sql, LogSystemFilterDao filterDao){
        boolean isEventColumn = filterDao.getSearchOrdering() == HistoryBusinessSearchOrdering.EVENT;
        if (isEventColumn) {
            try {
                getJdbcTemplate().execute("CREATE GLOBAL TEMPORARY TABLE event_map (event_id NUMBER, event_title CHAR(100)) ON COMMIT DELETE ROWS");
            } catch (Throwable e) {
                // Выкидывает исключение если таблица существует
            }

            getJdbcTemplate().execute(insertEventTitles());
        }

        switch (filterDao.getAuditFormTypeId() != null ? filterDao.getAuditFormTypeId() : 0){
            case 1:
                sql.append(" LEFT JOIN fdSelection fds ON  ls.department_id = fds.department_id AND ls.form_type_id = fds.form_type_id AND ls.form_kind_id = fds.kind");
                sql.append(" LEFT JOIN report_period rp ON rp.id = fds.report_period_id");
                break;
            case 2:
                sql.append(" LEFT JOIN ddSelection dds ON ls.department_id  = dds.department_id AND ls.declaration_type_id = dds.declaration_type_id");
                sql.append(" LEFT JOIN report_period rp ON rp.id = dds.report_period_id");
                break;
            default:
                sql.append(" LEFT JOIN fdSelection fds ON  ls.department_id = fds.department_id AND ls.form_type_id = fds.form_type_id AND ls.form_kind_id = fds.kind");
                sql.append(" LEFT JOIN ddSelection dds ON ls.department_id  = dds.department_id AND ls.declaration_type_id = dds.declaration_type_id");
                sql.append(" LEFT JOIN report_period rp ON rp.id = fds.report_period_id OR rp.id = dds.report_period_id");
        }

        sql.append(" LEFT JOIN tax_period tp ON tp.id = rp.tax_period_id");
        sql.append(" left join department dep on ls.department_id=dep.\"ID\" ");
        sql.append(" left join form_type ft on ls.form_type_id=ft.\"ID\" ");
        sql.append(" left join sec_user su on ls.user_id=su.\"ID\" ");
        if (isEventColumn) {
            sql.append(" LEFT JOIN event_map em ON ls.event_id=em.\"EVENT_ID\" ");
        }
        sql.append(" WHERE (ls.report_period_name = CAST(tp.year AS VARCHAR(4)) || ' ' || rp.name) ");
        sql.append(filterDao.getReportPeriodName() != null && !filterDao.getReportPeriodName().isEmpty() ?
                "AND ls.report_period_name LIKE :rpName ":"");
        sql.append(filterDao.getUserIds() != null && !filterDao.getUserIds().isEmpty() ?
                "AND ls.user_id in (:userIds) ":"");
        sql.append(filterDao.getFromSearchDate() != null && filterDao.getToSearchDate() != null ?
                " AND ls.log_date between :fromDate AND :endDate + interval '1' day" : "");
    }

    private List<String>  expressionForReportNames(TaxType taxType){
        try {
            return getJdbcTemplate().query("SELECT tp.year as tax_year, rp.name AS report_period_name FROM tax_period tp" +
                    " LEFT JOIN report_period rp ON rp.tax_period_id = tp.id WHERE tp.tax_type = \'" + taxType.getCode()+ "\'",
                    new RowMapper<String>() {
                        @Override
                        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return String.format(AuditService.RP_NAME_PATTERN,
                                    rs.getInt("tax_year"), rs.getString("report_period_name"));
                        }
                    });
        } catch (DataAccessException e){
            logger.error("Ошибка при формировании списка имен периодов.", e);
            throw new DaoException("Ошибка при формировании списка имен периодов.", e);
        }
    }

    //Поиск доступных налоговых форм для пользователя
    private String appendFromAndWhereClauseFD(LogSystemFilterDao filter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT fd.ID as form_data_id");
        sql.append(" FROM form_data fd, form_type ft, department dp, report_period rp, tax_period tp")
                .append(" WHERE EXISTS (SELECT 1 FROM FORM_TEMPLATE t WHERE t.id = fd.form_template_id AND t.type_id = ft.id)")
                .append(" AND dp.id = fd.department_id AND rp.id = fd.report_period_id AND tp.id=rp.tax_period_id");

        if (filter.getFormTypeIds() != null && !filter.getFormTypeIds().isEmpty()) {
            sql.append(" AND ft.id in ").append(transformToSqlInStatement(filter.getFormTypeIds()));
        }

        if (filter.getTaxTypes() != null && !filter.getTaxTypes().isEmpty()) {
            sql.append(" AND ft.tax_type in ").append(transformTaxTypeToSqlInStatement(filter.getTaxTypes()));
        }

        if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
            sql.append(" AND rp.id in ").append(transformToSqlInStatement(filter.getReportPeriodIds()));
        }

        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql.append(" AND fd.department_id in ").append(transformToSqlInStatement(filter.getDepartmentIds()));
        }

        if (filter.getFormDataKinds() != null && !filter.getFormDataKinds().isEmpty()) {
            sql.append(" AND fd.kind in ").append(transformFormKindsToSqlInStatement(filter.getFormDataKinds()));
        }
        return sql.toString();
    }

    private String appendFromAndWhereClauseDD(LogSystemFilterDao filterDao) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT dec.ID as declaration_data_id");
        sql.append(" FROM declaration_data dec, declaration_type dectype, department dp, report_period rp, tax_period tp")
                .append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
                .append(" AND dp.id = dec.department_id AND rp.id = dec.report_period_id AND tp.id=rp.tax_period_id");

        if (filterDao.getTaxTypes() != null && !filterDao.getTaxTypes().isEmpty()) {
            sql.append(" AND dectype.tax_type in ").append(transformTaxTypeToSqlInStatement(filterDao.getTaxTypes()));
        }

        if (filterDao.getReportPeriodIds() != null && !filterDao.getReportPeriodIds().isEmpty()) {
            sql.append(" AND rp.id in ").append(transformToSqlInStatement(filterDao.getReportPeriodIds()));
        }

        if (filterDao.getDepartmentIds() != null && !filterDao.getDepartmentIds().isEmpty()) {
            sql.append(" AND dec.department_id in ").append(transformToSqlInStatement(filterDao.getDepartmentIds()));
        }

        if (filterDao.getDeclarationTypeId() != null) {
            sql.append(" AND dectype.id = ").append(filterDao.getDeclarationTypeId());
        }
        return sql.toString();
    }
}
