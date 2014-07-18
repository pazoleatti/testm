package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
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
import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {

	@Override
	public PagingResult<LogSearchResultItem> getLogs(LogSystemFilter filter) {
        return getLogsBusiness(filter, null);
    }

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusiness(LogSystemFilter filter, List<Integer> departments) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("select ordDat.* from (select dat.*, rownum as rn from ( select ");
        ps.appendQuery("ls.id, ");
        ps.appendQuery("ls.log_date, ");
        ps.appendQuery("ls.ip, ");
        ps.appendQuery("ls.event_id, ");
        ps.appendQuery("ev.name event, ");
        ps.appendQuery("ls.user_login user_login, ");
        ps.appendQuery("ls.roles, ");
        ps.appendQuery("ls.department_name, ");
        ps.appendQuery("ls.report_period_name, ");
        ps.appendQuery("ls.declaration_type_name, ");
        ps.appendQuery("ls.form_type_name, ");
        ps.appendQuery("ls.form_kind_id, ");
        ps.appendQuery("fk.name form_kind_name, ");
        ps.appendQuery("ls.note, ");

        ps.appendQuery("ls.user_department_name ");
        ps.appendQuery(" from log_system ls ");

        ps.appendQuery("left join event ev on ls.event_id=ev.\"ID\" ");
        ps.appendQuery("left join form_kind fk on ls.form_kind_id=fk.\"ID\" ");

        if (departments != null) {
            ps.appendQuery(" WHERE (ls.form_type_name is not null OR ls.declaration_type_name is not null ) AND ");
            ps.appendQuery(transformToSqlInStatement("form_department_id", departments));
            appendSelectWhereClause(ps, filter, " AND");
        } else {
            appendSelectWhereClause(ps, filter, " WHERE");
        }

        ps.appendQuery(orderByClause(filter.getSearchOrdering(), filter.isAscSorting()));
		ps.appendQuery(") dat) ordDat");
        if(filter.getCountOfRecords() != 0){
            ps.appendQuery(" where ordDat.rn between ? and ?"+
                    " order by ordDat.rn");
            ps.addParam(filter.getStartIndex() + 1);
            ps.addParam(filter.getStartIndex() + filter.getCountOfRecords());
        } else{
            ps.appendQuery(" order by ordDat.rn");
        }
        List<LogSearchResultItem> records = getJdbcTemplate().query(
                ps.getQuery().toString(),
                ps.getParams().toArray(),
                new AuditRowMapper());
		return new PagingResult<LogSearchResultItem>(records, getCount(filter, departments));
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
                    "insert into log_system (id, log_date, ip, event_id, user_login, roles, department_name, report_period_name, " +
                            "declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id)" +
                            " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id,
                    logSystem.getLogDate(),
                    logSystem.getIp(),
                    logSystem.getEventId(),
                    logSystem.getUserLogin(),
                    logSystem.getRoles(),
                    logSystem.getFormDepartmentName(),
                    logSystem.getReportPeriodName(),
                    logSystem.getDeclarationTypeName(),
                    logSystem.getFormTypeName(),
                    logSystem.getFormKindId(),
                    logSystem.getNote(),
                    logSystem.getUserDepartmentName(),
                    logSystem.getFormDepartmentId()
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

    private void appendSelectWhereClause(PreparedStatementData ps, LogSystemFilter filter, String wa) {
        String prefix = "";
        ps.appendQuery(wa);
        ps.appendQuery(" (? is null or (log_date BETWEEN ? AND (? + interval '1' day)))");
        ps.addParam(filter.getFromSearchDate());
        ps.addParam(filter.getFromSearchDate());
        ps.addParam(filter.getToSearchDate());

        if (filter.getFilter() != null && !filter.getFilter().equals("")) {
            if (!filter.getAuditFieldList().isEmpty())
                ps.appendQuery(" AND (1<>1 ");

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.FORM_TYPE.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sform_type_name) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.DECLARATION_TYPE.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sdeclaration_type_name) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.PERIOD.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sreport_period_name) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.DEPARTMENT.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sdepartment_name) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.USER.getId()) ) {
                ps.appendQuery(" OR lower(ls.user_login) LIKE lower(?)");
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.ROLE.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sroles) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.EVENT.getId()) ) {
                ps.appendQuery(" OR lower(ev.name) LIKE lower(?)");
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.NOTE.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%snote) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.FORM_KIND.getId()) ) {
                ps.appendQuery(" OR lower(fk.name) LIKE lower(?)");
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (filter.getAuditFieldList().contains(AuditFieldList.ALL.getId())
                    || filter.getAuditFieldList().contains(AuditFieldList.IP.getId()) ) {
                ps.appendQuery(String.format(" OR lower(%sip) LIKE lower(?)", prefix));
                ps.addParam("%"+filter.getFilter()+"%");
            }

            if (!filter.getAuditFieldList().isEmpty())
                ps.appendQuery(")");
        }
        if (filter.getOldLogSystemFilter() != null) {
            appendSelectWhereClause(ps, filter.getOldLogSystemFilter(), " AND");
        }
    }

	private final class AuditRowMapper implements RowMapper<LogSearchResultItem> {
		@Override
		public LogSearchResultItem mapRow(ResultSet rs, int index) throws SQLException {
            LogSearchResultItem log = new LogSearchResultItem();
			log.setId(SqlUtils.getLong(rs,"id"));
			log.setLogDate(new Date(rs.getTimestamp("log_date").getTime()));
			log.setIp(rs.getString("ip"));
			log.setEvent(FormDataEvent.getByCode(SqlUtils.getInteger(rs, "event_id")));
			log.setUser(rs.getString("user_login"));
			log.setRoles(rs.getString("roles"));
            log.setDepartmentName(rs.getString("department_name"));
			log.setReportPeriodName(rs.getString("report_period_name"));
            log.setDeclarationTypeName(rs.getString("declaration_type_name"));
            log.setFormTypeName(rs.getString("form_type_name"));
            if (SqlUtils.getInteger(rs, "form_kind_id") != null) {
                log.setFormKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "form_kind_id")));
            }
			log.setNote(rs.getString("note"));
			log.setUserDepartmentName(rs.getString("user_department_name"));
			return log;
		}
	}

    private String appendWithClauseDepartment(List<Integer> departments){
        return "SELECT LTRIM(SYS_CONNECT_BY_PATH(name, '/'), '/') as path" +
                " FROM department" +
                " WHERE " +transformToSqlInStatement("id", departments) +
                " START WITH parent_id in (select id from department where parent_id is null)" +
                " CONNECT BY PRIOR id = parent_id";
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
                column = "ls.report_period_name";
                break;
            case DEPARTMENT:
                column = "ls.department_name";
                break;
            case TYPE:
                column = "CASE WHEN ls.declaration_type_name != NULL THEN ls.declaration_type_name ELSE ls.form_type_name END";
                break;
            case FORM_DATA_KIND:
                column = "ls.form_kind_id";
                break;
            case FORM_TYPE:
                column = "ft.form_type_name";
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


    private int getCount(LogSystemFilter filter, List<Integer> departments) {
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("select count(*) from log_system ls ");
        ps.appendQuery("left join event ev on ls.event_id=ev.\"ID\" ");
        /*ps.appendQuery("left join sec_user su on ls.user_id=su.\"ID\" ");*/
        ps.appendQuery("left join form_kind fk on ls.form_kind_id=fk.\"ID\" ");
        if (departments != null) {
            ps.appendQuery(" WHERE (ls.form_type_name is not null OR ls.declaration_type_name is not null ) AND ");
            ps.appendQuery(transformToSqlInStatement("form_department_id", departments));
            appendSelectWhereClause(ps, filter, " AND");
        } else {
            appendSelectWhereClause(ps, filter, " WHERE");
        }
        return getJdbcTemplate().queryForInt(
                ps.getQuery().toString(),
                ps.getParams().toArray()
        );
    }
}
