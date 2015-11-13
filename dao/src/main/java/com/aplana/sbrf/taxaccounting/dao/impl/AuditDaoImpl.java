package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.AuditDao;
import com.aplana.sbrf.taxaccounting.dao.EventDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import static com.aplana.sbrf.taxaccounting.model.FormDataEvent.*;

@Repository
public class AuditDaoImpl extends AbstractDao implements AuditDao {

	private static final Log LOG = LogFactory.getLog(AuditDaoImpl.class);

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

    //Запрос дя получения записей по ЖА для пользователей с ролями CONTROL, CONTROL_NS
    //Условия связанные с получением источников/приемников вынесены в отдельное представление
    // (subquery source_filter_2B, source_filter_4B, performer_filter).
    // Поскольку в постановке условия на записи ЖА сказано, что по выборкам 45, 55 должны совпадать источники/приемники,
    // то они так же вынесены в представления

    private static final String SQL_FILTERS_FOR_CONTROL =
            "WITH source_filter_2K AS (SELECT DISTINCT\n" +
                    "                         src.id            id,\n" +
                    "                         src.DEPARTMENT_ID department_id,\n" +
                    "                         src.FORM_TYPE_ID  form_type_id,\n" +
                    "                         src.KIND          kind,\n" +
                    "                         fds.PERIOD_START  start_date,\n" +
                    "                         fds.PERIOD_END    end_date\n" +
                    "                       FROM department_form_type src\n" +
                    "                         JOIN form_data_source fds ON src.id = fds.src_department_form_type_id\n" +
                    "                         JOIN department_form_type tgt ON fds.department_form_type_id = tgt.id\n" +
                    "                       WHERE %s and %s) --10\n" +
                    "  ,\n" +
                    "    source_filter_4K AS (SELECT DISTINCT\n" +
                    "                           src.id            id,\n" +
                    "                           src.DEPARTMENT_ID dep_id,\n" +
                    "                           src.FORM_TYPE_ID  form_type_id,\n" +
                    "                           src.KIND          kind,\n" +
                    "                           fds.PERIOD_START  start_date,\n" +
                    "                           fds.PERIOD_END    end_date\n" +
                    "                         FROM department_form_type src\n" +
                    "                           JOIN form_data_source fds ON src.id = fds.src_department_form_type_id\n" +
                    "                           JOIN department_form_type tgt ON fds.department_form_type_id = tgt.id\n" +
                    "                         WHERE %s and %s),--55 и 10\n" +
                    "    performer_filter_3K AS (SELECT\n" +
                    "                           src.id            id,\n" +
                    "                           src.FORM_TYPE_ID  form_type_id,\n" +
                    "                           src.KIND          kind,\n" +
                    "                           src.DEPARTMENT_ID dep_id\n" +
                    "                         FROM department_form_type src\n" +
                    "                         WHERE %s and %s) --55\n";

    private static final String SQL_CONTROL =
            SQL_FILTERS_FOR_CONTROL +
                    "SELECT\n" +
                    "  ordDat.*\n" +
                    "FROM (SELECT\n" +
                    "        dat.*,\n" +
                    "        count(*) OVER ()   cnt,\n" +
                    "        rownum AS rn\n" +
                    "      FROM (SELECT DISTINCT\n" +
                    "              ls.id,\n" +
                    "              ls.log_date,\n" +
                    "              ls.ip,\n" +
                    "              ls.event_id,\n" +
                    "              ev.name       event,\n" +
                    "              ls.user_login user_login,\n" +
                    "              ls.roles,\n" +
                    "              ls.department_name,\n" +
                    "              ls.report_period_name,\n" +
                    "              ls.declaration_type_name,\n" +
                    "              ls.form_type_name,\n" +
                    "              ls.form_kind_id,\n" +
                    "              ls.form_type_id,\n" +
                    "              fk.name       form_kind_name,\n" +
                    "              ls.note,\n" +
                    "              ls.user_department_name,\n" +
                    "              ls.blob_data_id\n" +
                    "            FROM log_system ls\n" +
                    "              LEFT JOIN event ev ON ls.event_id = ev.\"ID\"\n" +
                    "              LEFT JOIN form_kind fk ON ls.form_kind_id = fk.\"ID\"\n" +
                    "              LEFT JOIN source_filter_2K filter1 ON\n" +
                    "                                                   ls.form_kind_id = filter1.kind AND\n" +
                    "                                                   ls.FORM_TYPE_ID = filter1.form_type_id\n" +
                    "                                                   AND ls.LOG_DATE >= filter1.start_date AND\n" +
                    "                                                   (filter1.end_date IS NULL OR ls.LOG_DATE <= filter1.end_date) AND\n" +
                    "                                                   %s\n" +
                    "              LEFT JOIN performer_filter_3K filter2 ON ls.FORM_TYPE_ID = filter2.form_type_id AND\n" +
                    "                                                    ls.form_kind_id = filter2.kind AND\n" +
                    "                                                    filter2.dep_id = ls.FORM_DEPARTMENT_ID\n" +
                    "                                                    AND\n" +
                    "                                                    %s\n" +
                    "              LEFT JOIN source_filter_4K filter3 ON ls.FORM_TYPE_ID = filter3.form_type_id AND\n" +
                    "                                                    ls.form_kind_id = filter3.kind AND\n" +
                    "                                                    filter3.dep_id = ls.FORM_DEPARTMENT_ID\n" +
                    "                                                    AND\n" +
                    "                                                    %s\n" +
                    "            WHERE (\n" +
                    "              (%s AND %s)\n" +
                    "              OR\n" +
                    "              (%s AND %s))\n";

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusinessForControl(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        cutOffLogSystemFilter(filter);

        HashSet<Integer> s_45 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_45));
        HashSet<Integer> s_55 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_55));
        ArrayList<Integer> s_45_55 = new ArrayList<Integer>(s_45.size() + s_55.size());
        s_45_55.addAll(s_45);
        s_45_55.addAll(s_55);
        String sql = String.format(
                SQL_CONTROL,
                !s_45.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_45) : "1=3",
                SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%", "90%")),
                !s_45_55.isEmpty() ? SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", s_45_55) : "1=3",
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "90%"))
        );

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(sql);
        appendSelectWhereClause(ps, filter, " AND ");
        appendEndOrderClause(ps, filter);
        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String LOG_SYSTEM_DATA_FOR_CONTROL_BY_FILTER_COUNT = SQL_FILTERS_FOR_CONTROL +
            "select count(*) " +
            "from log_system ls " +
            "left join event ev on ls.event_id=ev.\"ID\" " +
            "left join form_kind fk on ls.form_kind_id=fk.\"ID\" " +
            "              LEFT JOIN source_filter_2K filter1 ON\n" +
            "                                                   ls.form_kind_id = filter1.kind AND\n" +
            "                                                   ls.FORM_TYPE_ID = filter1.form_type_id\n" +
            "                                                   AND ls.LOG_DATE >= filter1.start_date AND\n" +
            "                                                   (filter1.end_date IS NULL OR ls.LOG_DATE <= filter1.end_date) AND\n" +
            "                                                   %s\n" +
            "              LEFT JOIN performer_filter_3K filter2 ON ls.FORM_TYPE_ID = filter2.form_type_id AND\n" +
            "                                                    ls.form_kind_id = filter2.kind AND\n" +
            "                                                    filter2.dep_id = ls.FORM_DEPARTMENT_ID\n" +
            "                                                    AND\n" +
            "                                                    %s\n" +
            "              LEFT JOIN source_filter_4K filter3 ON ls.FORM_TYPE_ID = filter3.form_type_id AND\n" +
            "                                                    ls.form_kind_id = filter3.kind AND\n" +
            "                                                    filter3.dep_id = ls.FORM_DEPARTMENT_ID\n" +
            "                                                    AND\n" +
            "                                                    %s\n" +
            "            WHERE (\n" +
            "              (%s AND %s)\n" +
            "              OR\n" +
            "              (%s AND %s))\n";

    @Override
    public long getCountForControl(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        cutOffLogSystemFilter(filter);

        HashSet<Integer> s_45 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_45));
        HashSet<Integer> s_55 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_55));
        ArrayList<Integer> s_45_55 = new ArrayList<Integer>(s_45.size() + s_55.size());
        s_45_55.addAll(s_45);
        s_45_55.addAll(s_55);
        String sql = String.format(
                LOG_SYSTEM_DATA_FOR_CONTROL_BY_FILTER_COUNT,
                !s_45.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_45) : "1=3",
                SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("tgt.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "_", "10%", "40%", "90%")),
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", s_45_55),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_CONTROL, null, "90%"))
        );

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(sql);
        appendSelectWhereClause(ps, filter, " AND ");
        try{
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
        } catch (EmptyResultDataAccessException e){
            return 0;
        }
    }

    private static final String SQL_FILTERS_FOR_OPER =
            "WITH source_filter_2O AS (SELECT\n" +
                    "                            src.id            id,\n" +
                    "                            src.FORM_TYPE_ID  form_type_id,\n" +
                    "                            src.KIND          kind,\n" +
                    "                            src.DEPARTMENT_ID dep_id\n" +
                    "                          FROM department_form_type src\n" +
                    "                          WHERE %s and %s)\n";

    private static final String SQL_OPER = SQL_FILTERS_FOR_OPER +
                    "SELECT\n" +
                    "  ordDat.*\n" +
                    "FROM (SELECT\n" +
                    "        dat.*,\n" +
                    "        count(*) OVER () cnt,\n" +
                    "        rownum AS rn\n" +
                    "      FROM (SELECT DISTINCT\n" +
                    "              ls.id,\n" +
                    "              ls.log_date,\n" +
                    "              ls.ip,\n" +
                    "              ls.event_id,\n" +
                    "              ev.name       event,\n" +
                    "              ls.user_login user_login,\n" +
                    "              ls.roles,\n" +
                    "              ls.department_name,\n" +
                    "              ls.report_period_name,\n" +
                    "              ls.declaration_type_name,\n" +
                    "              ls.form_type_name,\n" +
                    "              ls.form_kind_id,\n" +
                    "              ls.form_type_id,\n" +
                    "              fk.name       form_kind_name,\n" +
                    "              ls.note,\n" +
                    "              ls.user_department_name,\n" +
                    "              ls.blob_data_id\n" +
                    "            FROM log_system ls LEFT JOIN event ev ON ls.event_id = ev.\"ID\"\n" +
                    "              LEFT JOIN form_kind fk ON ls.form_kind_id = fk.\"ID\"\n" +
                    "              LEFT JOIN source_filter_2O filter1 ON\n" +
                    "                                                   ls.form_kind_id = filter1.kind AND\n" +
                    "                                                   ls.FORM_TYPE_ID = filter1.form_type_id AND                                                   \n" +
                    "                                                   %s\n" +
                    "            WHERE (\n" +
                    "              (%s AND %s)\n" +
                    "              OR\n" +
                    "              (%s AND %s) )";

    @Override
    public PagingResult<LogSearchResultItem> getLogsBusinessForOper(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        HashSet<Integer> s_55 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_55));
        String sql = String.format(
                SQL_OPER,
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "_", "10%", "40%", "90%")),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "90%"))
                );
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(sql);
        appendSelectWhereClause(ps, filter, " AND ");
        appendEndOrderClause(ps, filter);

        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String LOG_SYSTEM_DATA_FOR_OPER_BY_FILTER_COUNT = SQL_FILTERS_FOR_OPER +
            "select count(*) " +
            "from log_system ls " +
            "left join event ev on ls.event_id=ev.\"ID\" " +
            "left join form_kind fk on ls.form_kind_id=fk.\"ID\" " +
            "              LEFT JOIN source_filter_2O filter1 ON\n" +
            "                                                   ls.form_kind_id = filter1.kind AND\n" +
            "                                                   ls.FORM_TYPE_ID = filter1.form_type_id AND                                                   \n" +
            "                                                   %s\n" +
            "            WHERE (\n" +
            "              (%s AND %s)\n" +
            "              OR\n" +
            "              (%s AND %s))\n";


    @Override
    public long getCountForOper(LogSystemFilter filter, Map<SAMPLE_NUMBER, Collection<Integer>> availableDepIds) {
        HashSet<Integer> s_55 = new HashSet<Integer>(availableDepIds.get(SAMPLE_NUMBER.S_55));
        String sql = String.format(
                LOG_SYSTEM_DATA_FOR_OPER_BY_FILTER_COUNT,
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("src.DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("src.performer_dep_id", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "_", "10%", "40%")),
                SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", availableDepIds.get(SAMPLE_NUMBER.S_10)),
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "_", "10%", "40%", "90%")),
                !s_55.isEmpty() ? SqlUtils.transformToSqlInStatement("ls.FORM_DEPARTMENT_ID", s_55) : "1=3",
                SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                        eventDao.getEventCodes(TARole.ROLE_OPER, null, "90%"))
        );
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(sql);

        appendSelectWhereClause(ps, filter, " AND ");
        try{
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
        } catch (EmptyResultDataAccessException e){
            return 0;
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
                eventDao.getEventCodes(TARole.ROLE_CONTROL, Arrays.asList(
								LOGIN.getCode(),
								LOGOUT.getCode(),
								LOG_SYSTEM_BACKUP.getCode(),
								TEMPLATE_CREATED.getCode()))));
        ps.appendQuery(" )");

        appendSelectWhereClause(ps, filter, " AND ");

        appendEndOrderClause(ps, filter);
        try {
            List<LogSearchResultItem> records = getJdbcTemplate().query(ps.getQuery().toString(),
                    ps.getParams().toArray(),
                    new AuditRowMapper());
            return new PagingResult<LogSearchResultItem>(records, !records.isEmpty()?records.get(0).getCnt():0);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public long getCountForControlUnp(LogSystemFilter filter) {
        cutOffLogSystemFilter(filter);

        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(LOG_SYSTEM_DATA_BY_FILTER_COUNT);

        ps.appendQuery("WHERE (");
        ps.appendQuery(SqlUtils.transformToSqlInStatement("ls.EVENT_ID",
                eventDao.getEventCodes(TARole.ROLE_CONTROL, Arrays.asList(
						LOGIN.getCode(),
						LOGOUT.getCode(),
						LOG_SYSTEM_BACKUP.getCode(),
						TEMPLATE_CREATED.getCode()))));
        ps.appendQuery(" )");

        appendSelectWhereClause(ps, filter, " AND ");
        try{
            return getJdbcTemplate().queryForObject(ps.getQuery().toString(), ps.getParams().toArray(), Long.class);
        } catch (EmptyResultDataAccessException e){
            return 0;
        }
    }


    private static final String LOG_SYSTEM_DATA_BY_FILTER_COUNT = "select count(*) " +
            "from log_system ls " +
            "left join event ev on ls.event_id=ev.\"ID\" " +
            "left join form_kind fk on ls.form_kind_id=fk.\"ID\" ";

    @Override
    public long getCount(final LogSystemFilter filter) {
        cutOffLogSystemFilter(filter);
        PreparedStatementData ps = new PreparedStatementData();
        ps.appendQuery(LOG_SYSTEM_DATA_BY_FILTER_COUNT);
        ps.appendQuery("WHERE ");
        appendSelectWhereClause(ps, filter, "");
        try{
            return getJdbcTemplate().queryForObject(
                    ps.getQuery().toString(),
                    ps.getParams().toArray(), Long.class);
        } catch (EmptyResultDataAccessException e){
            return 0;
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
                            " values (?, SYSDATE, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id,
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
			LOG.error("Ошибки при логировании.", e);
            throw new DaoException("Ошибки при логировании.", e);
        }
	}

    @Override
    public void removeRecords(List<Long> listIds) {
        JdbcTemplate jt = getJdbcTemplate();
        jt.update("delete from log_system where " + SqlUtils.transformToSqlInStatement("id", listIds));
    }


    private static final String DELETE_RECORDS_BY_FILTER =
            "delete from LOG_SYSTEM where id in (\n" +
                    "  select ls.id\n" +
                    "    from log_system ls \n" +
                    "    WHERE  log_date <= (:toDate + interval '1' day) \n" +
                    ")";

    @Override
    public void removeRecords(final LogSystemFilter filter) {
        getNamedParameterJdbcTemplate().update(
                DELETE_RECORDS_BY_FILTER,
                new HashMap<String, Object>() {{
                    put("toDate", filter.getToSearchDate());
                }});
    }

    @Override
    public Date lastArchiveDate() {
        try {
            return getJdbcTemplate().queryForObject("select max(log_date) from log_system where event_id = 601", Date.class);
        } catch (EmptyResultDataAccessException e){
			LOG.warn("Не найдено записей об архивации.", e);
            return null;
        } catch (IncorrectResultSizeDataAccessException e){
			LOG.error("Найдено больше одной записи об архивировании.", e);
            throw new DaoException("Найдено больше одной записи об архивировании.", e);
        } catch (DataAccessException e){
			LOG.error("Ошибка при получении даты последней архивации", e);
            throw new DaoException("Ошибка при получении даты последней архивации", e);
        }

    }

    @Override
    public Date firstDateOfLog() {
        try{
            return getJdbcTemplate().queryForObject("select min(log_date) from log_system", Date.class);
        } catch (EmptyResultDataAccessException e){
            LOG.warn("Нет записей в журнале аудита.", e);
            return null;
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
        switch (ordering == null ? HistoryBusinessSearchOrdering.DATE : ordering) {
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
            default:
                column = "ls.log_date";
                break;
        }
        if (column != null) {
            order.append(column);
            if (!ascSorting) {
                order.append(" DESC");
            }
            /*order.append(", ");*/
        }
        // Сортировка по умолчанию
        /*order.append("ls.id");
        if (!ascSorting) {
            order.append(" DESC");
        }*/
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
            ps.appendQuery(" where ordDat.rn between ? and ? order by ordDat.rn");
            ps.addParam(filter.getStartIndex() + 1);
            ps.addParam(filter.getStartIndex() + filter.getCountOfRecords());
        } else {
            ps.appendQuery(" order by ordDat.rn");
        }
    }
}