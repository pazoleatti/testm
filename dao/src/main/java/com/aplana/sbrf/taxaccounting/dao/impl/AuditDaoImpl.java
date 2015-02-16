package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.EventDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {

    private static final int FILTER_LENGTH = 1000;

    @Autowired
    private EventDao eventDao;

    private static final String LOG_SYSTEM_DATA_BY_FILTER = "select ordDat.* from (select dat.*, count(*) over() cnt, rownum as rn from ( select DISTINCT " +
            "ls.id, ls.log_date, ls.ip, ls.event_id, ev.name event, ls.user_login user_login, ls.roles, ls.department_name, " +
            "ls.report_period_name, ls.declaration_type_name, ls.form_type_name, ls.form_kind_id, ls.form_type_id, " +
            "fk.name form_kind_name, ls.note, %s ls.user_department_name, ls.blob_data_id " +
            "from log_system ls " +
            "left join event ev on ls.event_id=ev.\"ID\" " +
            "left join form_kind fk on ls.form_kind_id=fk.\"ID\" ";

	@Override
	public PagingResult<LogSearchResultItem> getLogsForAdmin(LogSystemFilter filter) {
        cutOffLogSystemFilter(filter);
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(
                String.format(LOG_SYSTEM_DATA_BY_FILTER,
                        filter.getSearchOrdering() == HistoryBusinessSearchOrdering.FORM_TYPE ?
                                "case when ls.declaration_type_name is not null then ls.declaration_type_name else ls.form_type_name end as type_name, "
                                :
                                ""
                ));
        ps.appendQuery("WHERE ");
        appendSelectWhereClause(ps, filter, "");

        appendEndOrderClause(ps, filter);
        List<LogSearchResultItem> records = getJdbcTemplate().query(
                ps.getQuery().toString(),
                ps.getParams().toArray(),
                new AuditRowMapper());
        return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
    }

    private static final String FILTER_BY_DEPARTMENT_SOURCES =
            "source_filter as (SELECT DISTINCT src.id id, src.DEPARTMENT_ID department_id, src.FORM_TYPE_ID form_type_id, src.KIND kind, fds.PERIOD_START start_date, fds.PERIOD_END end_date\n" +
                    "            from department_form_type src\n" +
                    "            join form_data_source fds on src.id = fds.src_department_form_type_id\n" +
                    "            join department_form_type tgt on fds.department_form_type_id = tgt.id\n" +
                    "               WHERE %s and %s)\n";

    private static final String FILTER_BY_DEPARTMENT_PERFORMER =
            "performer_filter as (" +
                    "SELECT src.PERFORMER_DEP_ID performer_dep_id from department_form_type src " +
                    "where %s)";

    private static final String DEPARTMENT_SOURCES_CLAUSE =
            "exists ( select 1 from source_filter filter1 where ls.form_kind_id = filter1.kind and ls.FORM_TYPE_ID = filter1.form_type_id\n" +
                    "    and (ls.LOG_DATE >= filter1.start_date and (filter1.end_date is NULL or ls.LOG_DATE <= filter1.end_date)) \n" +
                    "    and ls.FORM_DEPARTMENT_ID = filter1.department_id and %s)";

    private static final String DEPARTMENT_PERFORMER_CLAUSE =
            "exists (select 1 from performer_filter filter2 where filter2.performer_dep_id = ls.FORM_DEPARTMENT_ID and %s)";

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusinessForControl(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        cutOffLogSystemFilter(filter);

        ArrayList<Integer> s_45_s_55_join = new ArrayList<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_55).size() +
                availableDepIds.get(SAMPLE_NUMBER.S_45).size());
        s_45_s_55_join.addAll(availableDepIds.get(SAMPLE_NUMBER.S_55));
        s_45_s_55_join.addAll(availableDepIds.get(SAMPLE_NUMBER.S_45));
        //2б,4б
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("with ");
        //Судя по постановке, выборка SAMPLE_NUMBER.S_10 пустой быть не
        ps.appendQuery(String.format(FILTER_BY_DEPARTMENT_SOURCES,
                SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_45_s_55_join)));
        //3б
        ps.appendQuery(",\n");
        ps.appendQuery(String.format(FILTER_BY_DEPARTMENT_PERFORMER,
                SqlUtils.transformToSqlInStatement("src.PERFORMER_DEP_ID", availableDepIds.get(SAMPLE_NUMBER.S_10))));
        ps.appendQuery(
                String.format(LOG_SYSTEM_DATA_BY_FILTER,
                        filter.getSearchOrdering() == HistoryBusinessSearchOrdering.FORM_TYPE ?
                                "case when ls.declaration_type_name is not null then ls.declaration_type_name else ls.form_type_name end as type_name, "
                                :
                                ""
                ));

        ps.appendQuery("WHERE (");
        //Фильтрация 2а,4а
        ps.appendQuery(String.format(DEPARTMENT_SOURCES_CLAUSE,
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%"))));
        ps.appendQuery(" OR ");
        //Фильтрация 3а
        ps.appendQuery(String.format(DEPARTMENT_PERFORMER_CLAUSE,
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%"))));
        ps.appendQuery(" OR ");
        //1.Отображение всех событий с кодами 1, 2, 3, 6, 10*, 40*, 7, 90*
        ps.appendQuery(String.format("( %s AND %s )",
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%", "90%"))));
        ps.appendQuery(" OR ");
        //5.Отображение всех событий с кодами 90*
        ps.appendQuery(String.format("( %s AND %s )",
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", s_45_s_55_join),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "90%"))));
        ps.appendQuery(" )");
        appendSelectWhereClause(ps, filter, " AND ");

        appendEndOrderClause(ps, filter);
        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusinessForOper(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        //2б
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery("with ");
        ps.appendQuery(String.format(FILTER_BY_DEPARTMENT_SOURCES,
                SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                !availableDepIds.get(SAMPLE_NUMBER.S_55).isEmpty() ?
                        SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_55)) :
                        "1=1"));
        ps.appendQuery(
                String.format(LOG_SYSTEM_DATA_BY_FILTER,
                        filter.getSearchOrdering() == HistoryBusinessSearchOrdering.FORM_TYPE ?
                                "case when ls.declaration_type_name is not null then ls.declaration_type_name else ls.form_type_name end as type_name, "
                                :
                                ""
                ));
        ps.appendQuery("WHERE (");
        //Фильтрация 2а
        ps.appendQuery(String.format(DEPARTMENT_SOURCES_CLAUSE,
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%"))));
        ps.appendQuery(" OR ");
        //1.Отображение всех событий с кодами 1, 2, 3, 6, 10*, 40*, 7, 90*
        ps.appendQuery(String.format("( %s AND %s )",
                !availableDepIds.get(SAMPLE_NUMBER.S_10).isEmpty() ?
                        SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)) :
                        "1=1",
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%", "90%"))));
        ps.appendQuery(" OR ");
        //3.Отображение всех событий с кодами 90*
        ps.appendQuery(String.format("( %s AND %s )",
                !availableDepIds.get(SAMPLE_NUMBER.S_55).isEmpty() ?
                        SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_55)) :
                        "1=1",
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "90%"))));
        ps.appendQuery(" )");

        appendSelectWhereClause(ps, filter, " AND ");
        appendEndOrderClause(ps, filter);

        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusinessForControlUnp(LogSystemFilter filter) {
        cutOffLogSystemFilter(filter);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(
                String.format(LOG_SYSTEM_DATA_BY_FILTER,
                        filter.getSearchOrdering() == HistoryBusinessSearchOrdering.FORM_TYPE ?
                                "case when ls.declaration_type_name is not null then ls.declaration_type_name else ls.form_type_name end as type_name, "
                                :
                                ""
                ));

        ps.appendQuery("WHERE (");
        ps.appendQuery(SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                eventDao.getEventCodes(TARole.ROLE_CONTROL, Arrays.asList(501, 502, 601, 701))));
        ps.appendQuery(" )");

        appendSelectWhereClause(ps, filter, " AND ");

        appendEndOrderClause(ps, filter);
        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
            logger.error("", e);
            throw new DaoException("", e);
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
                    "insert into log_system (id, log_date, ip, event_id, user_login, roles, department_name, report_period_name, " +
                            "declaration_type_name, form_type_name, form_kind_id, note, user_department_name, form_department_id, " +
                            "blob_data_id, form_type_id)" +
                            " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                    logSystem.getFormDepartmentId(),
                    logSystem.getBlobDataId(),
                    logSystem.getFormTypeId()
            );
        } catch (DataAccessException e){
            logger.error("Ошибки при логировании.", e);
            throw new DaoException("Ошибки при логировании.", e);
        }
	}

    @Override
    public void removeRecords(List<Long> listIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.update("delete from log_system where " + SqlUtils.transformToSqlInStatement("id", listIds));
    }


    private final static String DELETE_RECORDS_BY_FILTER =
            "delete from LOG_SYSTEM where id in (\n" +
                    "  select ls.id\n" +
                    "    from log_system ls \n" +
                    "    WHERE  log_date <= :toDate \n" +
                    ")";

    @Override
    public void removeRecords(final LogSystemFilter filter) {
        getNamedParameterJdbcTemplate().update(
                DELETE_RECORDS_BY_FILTER,
                new HashMap<String, Object>(){{put("toDate", filter.getToSearchDate());}});
    }

    @Override
    public Date lastArchiveDate() {
        try {
            return getJdbcTemplate().queryForObject("select max(log_date) from log_system where event_id = 601", Date.class);
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
                    || filter.getAuditFieldList().contains(AuditFieldList.TYPE.getId())) {
                ps.appendQuery(String.format(" OR lower(case when ls.form_type_name is not null then '" +
                        AuditFormType.FORM_TYPE_TAX.getName() + "' when ls.declaration_type_name is not null then '" + AuditFormType.FORM_TYPE_DECLARATION.getName() + "' else '' end) LIKE lower(?)"));
                ps.addParam("%" + filter.getFilter() + "%");
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
			log.setBlobDataId(rs.getString("blob_data_id"));
            log.setFormTypeId(SqlUtils.getInteger(rs, "form_type_id"));
            if (isSupportOver())log.setCnt(SqlUtils.getInteger(rs, "cnt"));
			return log;
		}
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
                column = "ev.name";
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
                column = "type_name";
                break;
            case USER:
                column = "ls.user_login";
                break;
            case USER_ROLE:
                column = "ls.roles";
                break;
            case USER_DEPARTMENT:
                column = "ls.user_department_name";
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

    /**
     * Ограничить строку фильтра, длина которого больше 1000 символов.
     * @param logSystemFilter фильтр
     */
    private void cutOffLogSystemFilter(LogSystemFilter logSystemFilter) {
        String filter = logSystemFilter.getFilter();
        if (filter != null && filter.length() > FILTER_LENGTH) {
            String substring = filter.substring(0, FILTER_LENGTH);
            logSystemFilter.setFilter(substring);
        }
    }

    private void appendEndOrderClause(PreparedStatementData ps, LogSystemFilter filter) {
        ps.appendQuery(orderByClause(filter.getSearchOrdering(), filter.isAscSorting()));
        ps.appendQuery(") dat) ordDat");
        if (filter.getCountOfRecords() != 0) {
            ps.appendQuery(" where ordDat.rn between ? and ?" +
                    " order by ordDat.rn");
            ps.addParam(filter.getStartIndex() + 1);
            ps.addParam(filter.getStartIndex() + filter.getCountOfRecords());
        } else {
            ps.appendQuery(" order by ordDat.rn");
        }
    }
}
