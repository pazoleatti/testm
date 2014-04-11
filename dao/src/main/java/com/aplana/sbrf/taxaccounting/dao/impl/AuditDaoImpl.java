package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.*;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.*;

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
import java.util.List;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {
	@Autowired
	private TAUserDao userDao;
	@Autowired
	private DepartmentDao departmentDao;
	@Autowired
	private ReportPeriodDao reportPeriodDao;
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
        sql.append("ls.report_period_id, ");
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
        sql.append("left join REPORT_PERIOD rp on ls.report_period_id=rp.\"ID\" ");
        sql.append("left join TAX_PERIOD tp on rp.tax_period_id=tp.\"ID\" ");
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

		if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
            sql.append(String.format(" AND (%sreport_period_id = ", prefix)).append(filter.getReportPeriodIds().get(0));
			for (int i = 1; i < filter.getReportPeriodIds().size(); i++) {
				sql.append(String.format(" OR %sreport_period_id = ", prefix)).append(filter.getReportPeriodIds().get(i));
			}
            sql.append(")");
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
            sql.append(String.format(" AND %stax_type = ", prefix.equals("") ? "" : "tp.")).append("'").append(filter.getTaxType().getCode()).append("'");
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
			log.setReportPeriod(reportPeriodDao.get(rs.getInt("report_period_id")));
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
        sql.append("left join REPORT_PERIOD rp on ls.report_period_id=rp.\"ID\" ");
        sql.append("left join TAX_PERIOD tp on rp.tax_period_id=tp.\"ID\" ");
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

    public String orderByClause(HistoryBusinessSearchOrdering ordering, boolean ascSorting) {

        StringBuilder order = new StringBuilder();

        order.append(" order by ");

        String column = null;

        if (ordering == null) {
            ordering = HistoryBusinessSearchOrdering.ID;
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
            query.append("INTO event_map(event_id, event_title) VALUES (" + value.getCode() + ", '" + value.getTitle() + "') ");
        }
        query.append("SELECT * FROM DUAL ");

        return query.toString();
    }
}
