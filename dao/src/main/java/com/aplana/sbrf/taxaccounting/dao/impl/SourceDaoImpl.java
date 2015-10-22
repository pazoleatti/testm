package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DepartmentDao;
import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.api.DeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.FormTypeDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.FormatUtils;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.source.ConsolidatedInstance;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class SourceDaoImpl extends AbstractDao implements SourceDao {

    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private DepartmentReportPeriodDao departmentReportPeriodDao;
    @Autowired
    private FormTypeDao formTypeDao;
    @Autowired
    private DeclarationTypeDao declarationTypeDao;

    private String buildSourcePairsInQuery(String query, String pairNames, List<SourcePair> sourcePairs) {
        StringBuilder in = new StringBuilder();
        in.append("(");
        int maxSize = SqlUtils.IN_CAUSE_LIMIT;
        int counter = 0;
        int sum = 0;
        int size = sourcePairs.size();
        for (SourcePair pair : sourcePairs) {
            if (counter == 0) {
                in.append(pairNames).append(" in (");
            }
            in.append("(").append(pair.getSource()).append(",").append(pair.getDestination()).append(")");
            if (counter == maxSize - 1) {
                in.append(")");
                if (sum < size) {
                    in.append(" OR ");
                }
                counter = 0;
            } else {
                if (sum < size - 1) {
                    in.append(",");
                } else {
                    in.append(")");
                }
                counter++;
            }
            sum++;
        }
        in.append(")");
        return String.format(query, in.toString());
    }

    private String buildConsolidatedPairsInQuery(String pairNames, Set<ConsolidatedInstance> pairs) {
        StringBuilder in = new StringBuilder();
        in.append("(");
        int maxSize = SqlUtils.IN_CAUSE_LIMIT;
        int counter = 0;
        int sum = 0;
        int size = pairs.size();
        for (ConsolidatedInstance pair : pairs) {
            if (counter == 0) {
                in.append(pairNames).append(" in (");
            }
            in.append("(").append(pair.getSourceId()).append(",").append(pair.getId()).append(")");
            if (counter == maxSize - 1) {
                in.append(")");
                if (sum < size) {
                    in.append(" OR ");
                }
                counter = 0;
            } else {
                if (sum < size - 1) {
                    in.append(",");
                } else {
                    in.append(")");
                }
                counter++;
            }
            sum++;
        }
        in.append(")");
        return in.toString();
    }

    private final static String GET_FORM_INTERSECTIONS = "select distinct a.src_department_form_type_id as source, s_kind.name as s_kind, s_type.name as s_type, \n" +
            "a.department_form_type_id as destination, d_kind.name as d_kind, d_type.name as d_type, \n" +
            "a.period_start, a.period_end \n" +
            "from form_data_source a \n" +
            "join department_form_type s_dft on s_dft.id = src_department_form_type_id\n" +
            "join form_kind s_kind on s_kind.id = s_dft.kind\n" +
            "join form_type s_type on s_type.id = s_dft.form_type_id\n" +
            "join department_form_type d_dft on d_dft.id = department_form_type_id\n" +
            "join form_kind d_kind on d_kind.id = d_dft.kind\n" +
            "join form_type d_type on d_type.id = d_dft.form_type_id\n" +
            "where \n" +
            "%s --список пар \n" +
            "and (period_end >= :periodStart or period_end is null) --дата открытия периода \n" +
            "and (:periodEnd is null or period_start <= :periodEnd) --дата окончания периода (может быть передана null)\n" +
            "and (:excludedPeriodStart is null or (period_start, period_end) not in ((:excludedPeriodStart, :excludedPeriodEnd))) --исключить этот период";

    private final static String GET_DECLARATION_INTERSECTIONS = "select distinct a.src_department_form_type_id as source, s_kind.name as s_kind, s_type.name as s_type, \n" +
            "a.department_declaration_type_id as destination, null as d_kind, d_type.name as d_type, \n" +
            "a.period_start, a.period_end\n" +
            "from declaration_source a\n" +
            "join department_form_type s_dft on s_dft.id = src_department_form_type_id\n" +
            "join form_kind s_kind on s_kind.id = s_dft.kind\n" +
            "join form_type s_type on s_type.id = s_dft.form_type_id\n" +
            "join department_declaration_type d_ddt on d_ddt.id = department_declaration_type_id\n" +
            "join declaration_type d_type on d_type.id = d_ddt.declaration_type_id\n" +
            "where \n" +
            "%s --список пар\n" +
            "and (period_end >= :periodStart or period_end is null) --дата открытия периода\n" +
            "and (:periodEnd is null or period_start <= :periodEnd) --дата окончания периода (может быть передана null)\n" +
            "and (:excludedPeriodStart is null or (period_start, period_end) not in ((:excludedPeriodStart, :excludedPeriodEnd))) --исключить этот период";

    @Override
    public Map<SourcePair, List<SourceObject>> getIntersections(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd,
                                                                Date excludedPeriodStart, Date excludedPeriodEnd, final boolean declaration) {
        final Map<SourcePair, List<SourceObject>> result = new HashMap<SourcePair, List<SourceObject>>();
        //формируем in-часть вида ((1, 2), (1, 3))
        String sql;
        if (declaration) {
            sql = buildSourcePairsInQuery(GET_DECLARATION_INTERSECTIONS, "(src_department_form_type_id, department_declaration_type_id)", sourcePairs);
        } else {
            sql = buildSourcePairsInQuery(GET_FORM_INTERSECTIONS, "(src_department_form_type_id, department_form_type_id)", sourcePairs);
        }
        //составляем все параметры вместе. PreparedStatementData использовать не получается, т.к несколько параметров могут быть равны null
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("excludedPeriodStart", excludedPeriodStart);
        params.put("excludedPeriodEnd", excludedPeriodEnd);

        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                SourcePair pair = new SourcePair(rs.getLong("source"), rs.getLong("destination"));
                pair.setSourceKind(rs.getString("s_kind"));
                pair.setSourceType(rs.getString("s_type"));
                pair.setDestinationKind(rs.getString("d_kind"));
                pair.setDestinationType(rs.getString("d_type"));

                if (result.containsKey(pair)) {
                    result.get(pair).add(new SourceObject(pair, rs.getDate("period_start"), rs.getDate("period_end")));
                } else {
                    List<SourceObject> list = new ArrayList<SourceObject>();
                    list.add(new SourceObject(pair, rs.getDate("period_start"), rs.getDate("period_end")));
                    result.put(pair, list);
                }
            }
        });
        return result;
    }

    private final static String GET_LOOPS = "with subset(src, tgt, period_start, period_end, base) as\n" +
            "(\n" +
            "--существующие записи из таблицы соответствия\n" +
            "select tds.src_department_form_type_id, tds.department_form_type_id, period_start, period_end, 1 as base from form_data_source tds\n" +
            "union all --записи через cross join для возможности фильтрации попарно (можно заменить за запрос из временной таблицы), фиктивные даты = даты для фильтрации \n" +
            "select a.id, b.id, :periodStart, cast(:periodEnd as date), 0 as base from department_form_type a, department_form_type b where %s --список пар \n" +
            ")\n" +
            "select \n" +
            "       CONNECT_BY_ROOT src as IN_src_department_form_type_id, --исходный источник\n" +
            "       CONNECT_BY_ROOT tgt as IN_department_form_type_id, --исходный приемник\n" +
            "       src as src_department_form_type_id, \n" +
            "       tgt as department_form_type_id\n" +
            "from subset \n" +
            "where connect_by_iscycle = 1 -- есть зацикливание\n" +
            "connect by nocycle prior src = tgt and (period_end >= :periodStart or period_end is null) and (:periodEnd is null or period_start <= :periodEnd) -- предыдущий источник = текущему приемнику, т.е. поднимаемся наверх\n" +
            "start with base  = 0 -- начать с тех записей, которые были добавлены в граф фиктивно";

    @Override
    public Map<SourcePair, SourcePair> getLoops(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd) {
        final Map<SourcePair, SourcePair> result = new HashMap<SourcePair, SourcePair>();
        //составляем все параметры вместе. PreparedStatementData использовать не получается, т.к несколько параметров могут быть равны null
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);

        //формируем in-часть вида ((1, 2), (1, 3))
        String sql = buildSourcePairsInQuery(GET_LOOPS, "(a.id, b.id)", sourcePairs);
        getNamedParameterJdbcTemplate().query(sql, params, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(
                        new SourcePair(rs.getLong("in_src_department_form_type_id"), rs.getLong("in_department_form_type_id")),
                        new SourcePair(rs.getLong("src_department_form_type_id"), rs.getLong("department_form_type_id"))
                );
            }
        });
        return result;
    }

    private final class SourceBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
        private List<SourceObject> sources;
        private Date periodStart;
        private Date periodEnd;
        private boolean updateMode = false;
        private boolean needEndComparison = false;

        private SourceBatchPreparedStatementSetter(List<SourceObject> sources, boolean needEndComparison) {
            this.sources = sources;
            this.needEndComparison = needEndComparison;
        }

        private SourceBatchPreparedStatementSetter(List<SourceObject> sources, Date periodStart, Date periodEnd, boolean needEndComparison) {
            this.sources = sources;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            updateMode = true;
            this.needEndComparison = needEndComparison;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            SourceObject sourceObject = sources.get(i);
            java.sql.Date periodEndSql = sourceObject.getPeriodEnd() != null ? new java.sql.Date(sourceObject.getPeriodEnd().getTime()) : null;
            java.sql.Date newPeriodEndSql = periodEnd != null ? new java.sql.Date(periodEnd.getTime()) : null;
            if (updateMode) {
                ps.setDate(1, new java.sql.Date(periodStart.getTime()));
                ps.setDate(2, newPeriodEndSql);
                ps.setLong(3, sourceObject.getSourcePair().getSource());
                ps.setLong(4, sourceObject.getSourcePair().getDestination());
                ps.setDate(5, new java.sql.Date(sourceObject.getPeriodStart().getTime()));
                ps.setDate(6, periodEndSql);
                if (needEndComparison) {
                    ps.setDate(7, periodEndSql);
                }
            } else {
                ps.setLong(1, sourceObject.getSourcePair().getSource());
                ps.setLong(2, sourceObject.getSourcePair().getDestination());
                ps.setDate(3, new java.sql.Date(sourceObject.getPeriodStart().getTime()));
                ps.setDate(4, periodEndSql);
                if (needEndComparison) {
                    ps.setDate(5, periodEndSql);
                }
            }
        }

        @Override
        public int getBatchSize() {
            return sources.size();
        }
    }

    @Override
    public void deleteAll(final List<SourceObject> sources, boolean declaration) {
        if (declaration) {
            getJdbcTemplate().batchUpdate("delete from declaration_source where src_department_form_type_id = ? and department_declaration_type_id = ? and period_start = ? and ((? is null and period_end is null) or period_end = ?)",
                    new SourceBatchPreparedStatementSetter(sources, true));
        } else {
            getJdbcTemplate().batchUpdate("delete from form_data_source where src_department_form_type_id = ? and department_form_type_id = ? and period_start = ? and ((? is null and period_end is null) or period_end = ?)",
                    new SourceBatchPreparedStatementSetter(sources, true));
        }
    }

    @Override
    public void createAll(List<SourceObject> sources, boolean declaration) {
        if (declaration) {
            getJdbcTemplate().batchUpdate("insert into declaration_source (src_department_form_type_id, department_declaration_type_id, period_start, period_end) values (?,?,?,?)",
                    new SourceBatchPreparedStatementSetter(sources, false));
        } else {
            getJdbcTemplate().batchUpdate("insert into form_data_source (src_department_form_type_id, department_form_type_id, period_start, period_end) values (?,?,?,?)",
                    new SourceBatchPreparedStatementSetter(sources, false));
        }
    }

    @Override
    public void updateAll(List<SourceObject> sources, Date periodStart, Date periodEnd, boolean declaration) {
        if (declaration) {
            getJdbcTemplate().batchUpdate("update declaration_source set period_start = ?, period_end = ? where src_department_form_type_id = ? and department_declaration_type_id = ? and period_start = ? and ((? is null and period_end is null) or period_end = ?)",
                    new SourceBatchPreparedStatementSetter(sources, periodStart, periodEnd, true));
        } else {
            getJdbcTemplate().batchUpdate("update form_data_source set period_start = ?, period_end = ? where src_department_form_type_id = ? and department_form_type_id = ? and period_start = ? and ((? is null and period_end is null) or period_end = ?)",
                    new SourceBatchPreparedStatementSetter(sources, periodStart, periodEnd, true));
        }
    }

    private static final String GET_SOURCE_NAMES = "select dft.id, fk.name as form_kind, ft.name as form_type from department_form_type dft\n" +
            "join form_kind fk on fk.id = dft.kind\n" +
            "join form_type ft on ft.id = dft.form_type_id\n" +
            "where ";

    @Override
    public Map<Long, String> getSourceNames(List<Long> sourceIds) {
        final Map<Long, String> result = new HashMap<Long, String>();
        try {
            String sql = GET_SOURCE_NAMES + SqlUtils.transformToSqlInStatement("dft.id", sourceIds);
            getJdbcTemplate().query(sql, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    result.put(
                            rs.getLong("id"),
                            rs.getString("form_kind") + " - " + rs.getString("form_type")
                    );
                }
            });
            return result;
        } catch (EmptyResultDataAccessException e) {
            return new HashMap<Long, String>();
        }
    }

    @Override
    public List<Long> checkDFTExistence(List<Long> departmentFormTypeIds) {
        try {
            String sql = "select id from department_form_type where "+ SqlUtils.transformToSqlInStatement("id", departmentFormTypeIds);
            return getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    @Override
    public List<Long> checkDDTExistence(List<Long> departmentDeclarationTypeIds) {
        try {
            String sql = "select id from department_declaration_type where "+ SqlUtils.transformToSqlInStatement("id", departmentDeclarationTypeIds);
            return getJdbcTemplate().query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id");
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Long>();
        }
    }

    private static final String GET_EMPTY_PERIODS = "WITH \n" +
            "form_data_src AS\n" +
            "  (SELECT *\n" +
            "   FROM form_data_source\n" +
            "   WHERE department_form_type_id = :destination\n" +
            "     AND src_department_form_type_id = :source),    \n" +
            "form_data_src_extended (period_start, period_end) AS\n" +
            "  (SELECT period_end + interval '1' DAY, lead(period_start) over (ORDER BY period_start) - interval '1' DAY FROM form_data_src\n" +
            "   UNION ALL \n" +
            "   SELECT :newPeriodStart AS period_start, min(period_start) - interval '1' DAY\n" +
            "   FROM form_data_src -- дата начала нового периода\n" +
            "   UNION ALL \n" +
            "   SELECT max(coalesce(period_end, to_date('30.12.9999', 'DD.MM.YYYY'))) + interval '1' DAY AS period_start, to_date(:newPeriodEnd, 'DD.MM.YYYY') period_end FROM form_data_src -- дата окончания нового периода\n" +
            ")\n" +
            "SELECT *\n" +
            "FROM form_data_src_extended\n" +
            "WHERE period_end > period_start\n" +
            "ORDER BY 1";

    @Override
    public List<SourceObject> getEmptyPeriods(final SourcePair sourcePair, Date newPeriodStart, Date newPeriodEnd) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("source", sourcePair.getSource());
        params.put("destination", sourcePair.getDestination());
        params.put("newPeriodStart", newPeriodStart);
        params.put("newPeriodEnd", newPeriodEnd != null ? new SimpleDateFormat("dd.MM.yyyy").format(newPeriodEnd) : null);
        return getNamedParameterJdbcTemplate().query(GET_EMPTY_PERIODS, params, new RowMapper<SourceObject>() {
            @Override
            public SourceObject mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new SourceObject(sourcePair, rs.getDate("period_start"), rs.getDate("period_end"));
            }
        });
    }

    private static final String FIND_CONSOLIDATED_FORMS =
            "SELECT tfd.id as form_data_id, fd.id as source_form_data_id, tfd.kind AS kind, tfmt.name AS type, td.name AS department, trp.name AS period, ttp.year AS year, tfd.period_order AS month, tdrp.correction_date AS correctionDate, tfd.COMPARATIVE_DEP_REP_PER_ID as drpCompareId \n" +
                    "FROM department_form_type dft\n" +
                    "JOIN form_template ft ON ft.type_id = dft.form_type_id\n" +
                    "JOIN department_report_period drp ON drp.department_id = dft.department_id\n" +
                    "JOIN form_data fd ON (fd.kind = dft.kind AND fd.form_template_id = ft.id AND fd.department_report_period_id = drp.id)\n" +
                    "JOIN form_data_consolidation fdc ON fdc.source_form_data_id = fd.id\n" +
                    "JOIN form_data tfd ON tfd.id = fdc.target_form_data_id\n" +
                    "JOIN form_template tft ON tft.id = tfd.form_template_id\n" +
                    "JOIN form_type tfmt ON tfmt.id = tft.type_id\n" +
                    "JOIN department_report_period tdrp ON tdrp.id = tfd.department_report_period_id\n" +
                    "JOIN department td ON td.id = tdrp.department_id\n" +
                    "JOIN report_period trp ON trp.id = tdrp.report_period_id\n" +
                    "JOIN tax_period ttp ON ttp.id = trp.tax_period_id\n" +
                    "JOIN department_form_type tdft ON (tdft.form_type_id = tfmt.id AND tdft.department_id = td.id AND tdft.kind = tfd.kind)\n" +
                    "WHERE dft.id = :source AND tdft.id = :destination AND (\n" +
                    "(:periodStart <= trp.calendar_start_date AND (:periodEnd IS NULL OR :periodEnd >= trp.calendar_start_date)) OR\n" +
                    "(:periodStart >= trp.calendar_start_date AND :periodStart <= trp.end_date)\n" +
                    ")";

    private static final String FIND_CONSOLIDATED_DECLARATIONS =
            "select tdd.id as declaration_id, fd.id as source_form_data_id, dt.name as type, td.name as department, trp.name as period, ttp.year as year, tdrp.correction_date as correctionDate, tdd.tax_organ_code as taxOrganCode, tdd.kpp as kpp \n" +
            "from department_form_type dft\n" +
            "join form_template ft on ft.type_id = dft.form_type_id\n" +
            "join department_report_period drp on drp.department_id = dft.department_id\n" +
            "join form_data fd on (fd.kind = dft.kind and fd.form_template_id = ft.id and fd.department_report_period_id = drp.id)\n" +
            "join declaration_data_consolidation ddc on ddc.source_form_data_id = fd.id\n" +
            "join declaration_data tdd on tdd.id = ddc.target_declaration_data_id\n" +
            "join declaration_template tdt on tdt.id = tdd.declaration_template_id\n" +
            "join declaration_type dt on dt.id = tdt.declaration_type_id\n" +
            "join department_report_period tdrp on tdrp.id = tdd.department_report_period_id\n" +
            "join department td on td.id = tdrp.department_id\n" +
            "join report_period trp on trp.id = tdrp.report_period_id\n" +
            "join tax_period ttp on ttp.id = trp.tax_period_id\n" +
            "join department_declaration_type tddt on (tddt.declaration_type_id = dt.id and tddt.department_id = td.id)\n" +
            "where dft.id = :source and tddt.id = :destination and (\n" +
            "(:periodStart <= trp.calendar_start_date and (:periodEnd is null or :periodEnd >= trp.calendar_start_date)) or\n" +
            "(:periodStart >= trp.calendar_start_date and :periodStart <= trp.end_date)\n" +
            ")";

    @Override
    public List<ConsolidatedInstance> findConsolidatedInstances(long source, long destination, Date periodStart, Date periodEnd, boolean declaration) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("source", source);
        params.put("destination", destination);
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        List<ConsolidatedInstance> formsAndDeclarations = new ArrayList<ConsolidatedInstance>();

        if (declaration) {
            /** Получаем декларации, которые консолидируются из указанного источника */
            formsAndDeclarations.addAll(getNamedParameterJdbcTemplate().query(FIND_CONSOLIDATED_DECLARATIONS, params, new RowMapper<ConsolidatedInstance>() {
                @Override
                public ConsolidatedInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ConsolidatedInstance declaration = new ConsolidatedInstance();
                    declaration.setId(rs.getLong("declaration_id"));
                    declaration.setSourceId(rs.getLong("source_form_data_id"));
                    declaration.setType(rs.getString("type"));
                    declaration.setDepartment(rs.getString("department"));
                    declaration.setPeriod(rs.getString("period") + " " + rs.getInt("year"));
                    declaration.setCorrectionDate(rs.getDate("correctionDate"));
                    declaration.setDeclaration(true);
                    declaration.setTaxOrganCode(rs.getString("taxOrganCode"));
                    declaration.setKpp(rs.getString("kpp"));
                    return declaration;
                }
            }));
        } else {
            /** Получаем формы, которые консолидируются из указанного источника */
            formsAndDeclarations.addAll(getNamedParameterJdbcTemplate().query(FIND_CONSOLIDATED_FORMS, params, new RowMapper<ConsolidatedInstance>() {
                @Override
                public ConsolidatedInstance mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ConsolidatedInstance form = new ConsolidatedInstance();
                    form.setId(rs.getLong("form_data_id"));
                    form.setSourceId(rs.getLong("source_form_data_id"));
                    form.setFormKind(rs.getInt("kind"));
                    form.setType(rs.getString("type"));
                    form.setDepartment(rs.getString("department"));
                    form.setPeriod(rs.getString("period") + " " + rs.getInt("year"));
                    form.setDrpComapreId(rs.getInt("drpCompareId"));
                    form.setCorrectionDate(rs.getDate("correctionDate"));
                    form.setMonth(SqlUtils.getInteger(rs, "month"));
                    return form;
                }
            }));
        }
        return formsAndDeclarations;
    }

    private static final String GET_DEPARTMENT_NAMES = "select dft.id, d.name from department d \n" +
            "join department_form_type dft on dft.department_id = d.id \n" +
            "where %s";

    @Override
    public Map<Long, String> getDepartmentNamesBySource(List<Long> sources) {
        final Map<Long, String> result = new HashMap<Long, String>();
        String sql = String.format(GET_DEPARTMENT_NAMES, SqlUtils.transformToSqlInStatement("dft.id", sources));
        getJdbcTemplate().query(sql, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                result.put(rs.getLong("id"), rs.getString("name"));
            }
        });
        return result;
    }

    @Override
    public Integer getDepartmentIdByDepartmentFormType(long departmentFormTypeId) {
        return getJdbcTemplate().queryForObject(
                "select d.id from department d join department_form_type dft on dft.department_id = d.id where dft.id = ?",
                Integer.class,
                departmentFormTypeId);
    }

    @Override
    public Integer getDepartmentIdByDepartmentDeclarationType(long departmentDeclarationTypeId) {
        return getJdbcTemplate().queryForObject(
                "select d.id from department d join department_declaration_type ddt on ddt.department_id = d.id where ddt.id = ?",
                Integer.class,
                departmentDeclarationTypeId);
    }

    private static final String ADD_DECLARATION_CONSOLIDATION =
            "insert into DECLARATION_DATA_CONSOLIDATION (TARGET_DECLARATION_DATA_ID, SOURCE_FORM_DATA_ID) values (?,?)";

    @Override
    public void addDeclarationConsolidationInfo(final Long tgtDeclarationId, Collection<Long> srcFormDataIds) {
        final Object[] srcArray = srcFormDataIds.toArray();
        try{
            getJdbcTemplate().batchUpdate(ADD_DECLARATION_CONSOLIDATION, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, tgtDeclarationId);
                    ps.setLong(2, (Long)srcArray[i]);
                }

                @Override
                public int getBatchSize() {
                    return srcArray.length;
                }
            });
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    public void deleteDeclarationConsolidateInfo(long targetDeclarationDataId) {
        getJdbcTemplate().update("delete from DECLARATION_DATA_CONSOLIDATION where TARGET_DECLARATION_DATA_ID = ?",
                targetDeclarationDataId);
    }

    @Override
    public boolean isDeclarationSourceConsolidated(long declarationId, long sourceFormDataId) {
        try {
            getJdbcTemplate().queryForObject(
                    "select 1 from DECLARATION_DATA_CONSOLIDATION where target_declaration_data_id = ? and source_form_data_id = ?",
                    Integer.class,
                    declarationId, sourceFormDataId);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }

        return true;
    }

    private static final String ADD_CONSOLIDATION =
            "insert into FORM_DATA_CONSOLIDATION (TARGET_FORM_DATA_ID, SOURCE_FORM_DATA_ID) values (?,?)";
    @Override
    public void addFormDataConsolidationInfo(final Long tgtFormDataId, Collection<Long> srcFormDataIds) {
        final Object[] srcArray = srcFormDataIds.toArray();
        try{
            getJdbcTemplate().batchUpdate(ADD_CONSOLIDATION, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, tgtFormDataId);
                    ps.setLong(2, (Long)srcArray[i]);
                }

                @Override
                public int getBatchSize() {
                    return srcArray.length;
                }
            });
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    private static final String DELETE_CONSOLIDATION =
            "delete from FORM_DATA_CONSOLIDATION where TARGET_FORM_DATA_ID = ?";

    @Override
    public void deleteFormDataConsolidationInfo(final Collection<Long> tgtFormDataIds) {
        try{
            getJdbcTemplate().batchUpdate(DELETE_CONSOLIDATION, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, (Long)tgtFormDataIds.toArray()[i]);
                }

                @Override
                public int getBatchSize() {
                    return tgtFormDataIds.size();
                }
            });
        } catch (DataAccessException e){
            throw new DaoException("", e);
        }
    }

    @Override
    public boolean isFDSourceConsolidated(long formDataId, long sourceFormDataId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select 1 from FORM_DATA_CONSOLIDATION where TARGET_FORM_DATA_ID = ? and source_form_data_id = ?",
                    Integer.class,
                    formDataId, sourceFormDataId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void updateFDConsolidationInfo(long sourceFormId) {
        getJdbcTemplate().update(
                "update FORM_DATA_CONSOLIDATION set source_form_data_id = null where source_form_data_id = ?",
                sourceFormId);
    }

    @Override
    public void updateConsolidationInfo(Set<ConsolidatedInstance> instances, boolean declaration) {
        String sql;
        if (declaration) {
            sql = "update DECLARATION_DATA_CONSOLIDATION set source_form_data_id = null where " + buildConsolidatedPairsInQuery("(source_form_data_id, target_declaration_data_id)", instances);
            getJdbcTemplate().update(sql);
        } else {
            sql = "update FORM_DATA_CONSOLIDATION set source_form_data_id = null where " + buildConsolidatedPairsInQuery("(source_form_data_id, target_form_data_id)", instances);
        }
        getJdbcTemplate().update(sql);
    }

    @Override
    public int updateDDConsolidationInfo(long sourceFormId) {
        return getJdbcTemplate().update(
                "update DECLARATION_DATA_CONSOLIDATION set source_form_data_id = null where source_form_data_id = ?",
                sourceFormId);
    }

    @Override
    public boolean isFDConsolidationTopical(long fdTargetId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from form_data_consolidation where TARGET_FORM_DATA_ID = ? and SOURCE_FORM_DATA_ID is null",
                Integer.class, fdTargetId) == 0;
    }

    @Override
    public boolean isDDConsolidationTopical(long ddTargetId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from DECLARATION_DATA_CONSOLIDATION where TARGET_DECLARATION_DATA_ID = ? and SOURCE_FORM_DATA_ID is null",
                Integer.class, ddTargetId) == 0;
    }

    private static final String GET_SOURCES_INFO = "with insanity as \n" +
            "     (\n" +
            "     select sfd.id, sd.id as departmentId, sd.name as departmentName, sdrp.id as departmentReportPeriod, stp.YEAR, srp.name as periodName, \n" +
            "     sdrp.CORRECTION_DATE, sfd.state, sft.status as templateState, sfd.manual, \n" +
            "     st.id as formTypeId, st.name as formTypeName, sfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, \n" +
            "     --Если искомый экземпляр создан, то берем его значения периода и признака. \n" +
            "     --Если не создан и в его макете есть признак сравнения, то период и признак такой же как у источника\n" +
            "     --Если не создан и в его макете нет признаков сравнения, то период и признак пустой\n" +
            "     case when (sfd.id is not null) then scdrp.id when (sft.COMPARATIVE = 1) then cdrp.id else null end as compPeriodId,\n" +
            "     case when (sfd.id is not null) then sctp.year when (sft.COMPARATIVE = 1) then ctp.year else null end as compPeriodYear,\n" +
            "     case when (sfd.id is not null) then scrp.CALENDAR_START_DATE when (sft.COMPARATIVE = 1) then crp.CALENDAR_START_DATE else null end as compPeriodStartDate,\n" +
            "     case when (sfd.id is not null) then scrp.name when (sft.COMPARATIVE = 1) then crp.name else null end as compPeriodName,\n" +
            "     case when (sfd.id is not null) then sfd.ACCRUING when (sft.ACCRUING = 1) then fd.ACCRUING else null end as ACCRUING,\n" +
            "     case when sft.MONTHLY=1 then perversion.month else sfd.PERIOD_ORDER end as month, sdft.id as sdft_id\n" +
            "      from form_data fd \n" +
            "      join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and fd.id = :destinationFormDataId\n" +
            "      join report_period rp on rp.id = drp.REPORT_PERIOD_ID\n" +
            "      left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID\n" +
            "      left join report_period crp on crp.id = cdrp.REPORT_PERIOD_ID\n" +
            "      left join tax_period ctp on ctp.id = crp.TAX_PERIOD_ID\n" +
            "      join form_template ft on ft.id = fd.FORM_TEMPLATE_ID\n" +
            "      join department_form_type dft on (dft.DEPARTMENT_ID = drp.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)\n" +
            "      --ограничиваем назначения по пересечению с периодом приемника\n" +
            "      join form_data_source fds on (fds.DEPARTMENT_FORM_TYPE_ID = dft.id and ((fds.period_end >= rp.CALENDAR_START_DATE or fds.period_end is null) and fds.period_start <= rp.END_DATE))\n" +
            "      join department_form_type sdft on sdft.id = fds.SRC_DEPARTMENT_FORM_TYPE_ID\n" +
            "      join form_type st on st.id = sdft.FORM_TYPE_ID\n" +
            "      join form_kind sfk on sfk.ID = sdft.KIND\n" +
            "      --отбираем источники у которых дата корректировки ближе всего\n" +
            "      join department_report_period sdrp on (sdrp.DEPARTMENT_ID = sdft.DEPARTMENT_ID and sdrp.REPORT_PERIOD_ID = drp.REPORT_PERIOD_ID and nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) <= nvl(drp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))\n" +
            "      join department sd on sd.id = sdrp.DEPARTMENT_ID\n" +
            "      join report_period srp on srp.id = sdrp.REPORT_PERIOD_ID\n" +
            "      join tax_period stp on stp.ID = srp.TAX_PERIOD_ID\n" +
            "      --отбираем макет действующий для приемника в периоде приемника\n" +
            "      join form_template sft on (sft.TYPE_ID = st.ID and sft.status in (0,1) and sft.version = (select max(ft2.version) from form_template ft2 where ft2.TYPE_ID = sft.TYPE_ID and extract(year from ft2.version) <= stp.year))\n" +
            "      --если макет источника ежемесячный, то отбираем все возможные месяца для него из справочника\n" +
            "      left join\n" +
            "           (\n" +
            "           select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (      \n" +
            "              select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (\n" +
            "                        select r.id, v.date_value, a.alias from ref_book_value v \n" +
            "                        join ref_book_record r on r.id = v.record_id\n" +
            "                        join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')\n" +
            "                        where r.ref_book_id = 8)\n" +
            "                      pivot\n" +
            "                      (\n" +
            "                        max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)\n" +
            "                      )) t\n" +
            "            join (\n" +
            "                 select level i\n" +
            "                 from dual\n" +
            "                 connect by level <= 12\n" +
            "            ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2 \n" +
            "           ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and perversion.lvl = case when (sft.MONTHLY=1 and ft.MONTHLY=0) then perversion.lvl else 1 end\n" +
            "      --отбираем экземпляры с учетом периода сравнения, признака нарастающего истога, списка месяцов     \n" +
            "      left join form_data sfd on (sfd.kind = sfk.id and sfd.FORM_TEMPLATE_ID = sft.id and sfd.DEPARTMENT_REPORT_PERIOD_ID = sdrp.id \n" +
            "        and (sft.COMPARATIVE = 0 or ft.COMPARATIVE = 0 or sfd.COMPARATIVE_DEP_REP_PER_ID = fd.COMPARATIVE_DEP_REP_PER_ID) and (sft.ACCRUING = 0 or ft.ACCRUING = 0 or sfd.ACCRUING = fd.ACCRUING)) \n" +
            "        and coalesce(sfd.PERIOD_ORDER, perversion.month) = perversion.month\n" +
            "      left join form_data_performer fdp on fdp.form_data_id = sfd.id \n" +
            "      left join department fdpd on fdpd.id = fdp.PRINT_DEPARTMENT_ID      \n" +
            "      left join department_report_period scdrp on scdrp.id = sfd.COMPARATIVE_DEP_REP_PER_ID\n" +
            "      left join report_period scrp on scrp.id = scdrp.REPORT_PERIOD_ID\n" +
            "      left join tax_period sctp on sctp.id = scrp.TAX_PERIOD_ID\n" +
            "           ),\n" +
            "  aggregated_insanity as (\n" +
            "      select sdft_id, max(correction_date) as last_correction_date\n" +
            "      from insanity i\n" +
            "      where id is not null\n" +
            "      group by sdft_id\n" +
            "  )         \n" +
            "select id, departmentId, departmentName, correction_date, departmentReportPeriod, periodName, year, state, " +
            "templateState, formTypeId, formTypeName, formDataKind, performerId, performerName, " +
            "compPeriodId, compPeriodName, compPeriodYear, compPeriodStartDate, accruing, month, manual \n" +
            "       from insanity i\n" +
            "       left join aggregated_insanity i_agg on i.sdft_id = i_agg.sdft_id \n" +
            "       where nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(i_agg.last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) and (:excludeIfNotExist != 1 or id is not null) and (id is null or :stateRestriction is null or state = :stateRestriction)\n" +
            "       order by formTypeName, state, departmentName, id";

    @Override
    public List<Relation> getSourcesInfo(long destinationFormDataId, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("destinationFormDataId", destinationFormDataId);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            return getNamedParameterJdbcTemplate().query(GET_SOURCES_INFO, params, new RowMapper<Relation>() {
                @Override
                public Relation mapRow(ResultSet rs, int i) throws SQLException {
                    Relation relation =  mapFormCommon(rs, light, false);
                    relation.setSource(true);
                    return relation;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    private static final String GET_DESTINATIONS_INFO = "with insanity as \n" +
            "     (\n" +
            "     select tfd.id, td.id as departmentId, td.name as departmentName, tdrp.id as departmentReportPeriod, ttp.YEAR, trp.id as reportperiodid, trp.name as periodName, \n" +
            "     tdrp.CORRECTION_DATE, tfd.state, tft.status as templateState, tfd.manual,\n" +
            "     tt.id as formTypeId, tt.name as formTypeName, tfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, \n" +
            "     --Если искомый экземпляр создан, то берем его значения периода и признака. \n" +
            "     --Если не создан и в его макете есть признак сравнения, то период и признак такой же как у источника\n" +
            "     --Если не создан и в его макете нет признаков сравнения, то период и признак пустой\n" +
            "     case when (tfd.id is not null) then tcdrp.id when (tft.COMPARATIVE = 1) then cdrp.id else null end as compPeriodId,\n" +
            "     case when (tfd.id is not null) then tctp.year when (tft.COMPARATIVE = 1) then ctp.year else null end as compPeriodYear,\n" +
            "     case when (tfd.id is not null) then tcrp.CALENDAR_START_DATE when (tft.COMPARATIVE = 1) then crp.CALENDAR_START_DATE else null end as compPeriodStartDate,\n" +
            "     case when (tfd.id is not null) then tcrp.name when (tft.COMPARATIVE = 1) then crp.name else null end as compPeriodName,\n" +
            "     case when (tfd.id is not null) then tfd.ACCRUING when (tft.ACCRUING = 1) then fd.ACCRUING else null end as ACCRUING,\n" +
            "     case when tft.MONTHLY=1 then perversion.month else tfd.PERIOD_ORDER end as month\n" +
            "      from (\n" +
            "           select neighbours_fd.id, \n" +
            "                  neighbours_fd.form_template_id,\n" +
            "                  neighbours_fd.kind, \n" +
            "                  neighbours_fd.COMPARATIVE_DEP_REP_PER_ID,\n" +
            "                  neighbours_fd.ACCRUING, \n" +
            "                  neighbours_drp.department_id, \n" +
            "                  neighbours_drp.correction_date, \n" +
            "                  neighbours_drp.report_period_id,\n" +
            "                  coalesce(lag(neighbours_drp.correction_date) over (partition by neighbours_drp.department_id order by neighbours_drp.correction_date desc nulls last), to_date('31.12.9999', 'DD.MM.YYYY')) as next_correction_date\n" +
            "            from form_data fd \n" +
            "            join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and fd.id = :sourceFormDataId\n" +
            "            join department_report_period neighbours_drp on neighbours_drp.report_period_id = drp.report_period_id      \n" +
            "            join form_data neighbours_fd on neighbours_fd.department_report_period_id = neighbours_drp.id and neighbours_fd.form_template_id = fd.form_template_id and neighbours_fd.kind = fd.kind     \n" +
            "            ) fd             \n" +
            "      join report_period rp on rp.id = fd.REPORT_PERIOD_ID\n" +
            "      left join department_report_period cdrp on cdrp.id = fd.COMPARATIVE_DEP_REP_PER_ID\n" +
            "      left join report_period crp on crp.id = cdrp.REPORT_PERIOD_ID\n" +
            "      left join tax_period ctp on ctp.id = crp.TAX_PERIOD_ID\n" +
            "      join form_template ft on ft.id = fd.FORM_TEMPLATE_ID\n" +
            "      join department_form_type dft on (dft.DEPARTMENT_ID = fd.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)\n" +
            "      --ограничиваем назначения по пересечению с периодом приемника\n" +
            "      join form_data_source fds on (fds.SRC_DEPARTMENT_FORM_TYPE_ID = dft.id and ((fds.period_end >= rp.CALENDAR_START_DATE or fds.period_end is null) and fds.period_start <= rp.END_DATE))\n" +
            "      join department_form_type tdft on tdft.id = fds.DEPARTMENT_FORM_TYPE_ID\n" +
            "      join form_type tt on tt.id = tdft.FORM_TYPE_ID\n" +
            "      join form_kind tfk on tfk.ID = tdft.KIND\n" +
            "      --отбираем приемники у которых дата корректировки попадает в период действия даты корректировки от текущего источника и до следующего (с большей датой корректировки)\n" +
            "      join department_report_period tdrp on (tdrp.DEPARTMENT_ID = tdft.DEPARTMENT_ID and tdrp.REPORT_PERIOD_ID = fd.REPORT_PERIOD_ID and nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) between nvl(fd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) and fd.NEXT_CORRECTION_DATE - 1)\n" +
            "      join department td on td.id = tdrp.DEPARTMENT_ID\n" +
            "      join report_period trp on trp.id = tdrp.REPORT_PERIOD_ID\n" +
            "      join tax_period ttp on ttp.ID = trp.TAX_PERIOD_ID\n" +
            "      --отбираем макет действующий для приемника в периоде источника\n" +
            "      join form_template tft on (tft.TYPE_ID = tt.ID and tft.status in (0,1) and tft.version = (select max(ft2.version) from form_template ft2 where ft2.TYPE_ID = tft.TYPE_ID and extract(year from ft2.version) <= ttp.year)) \n" +
            "      --если макет приемника ежемесячный, то отбираем все возможные месяца для него из справочника\n" +
            "      left join\n" +
            "           (\n" +
            "           select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (      \n" +
            "              select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (\n" +
            "                        select r.id, v.date_value, a.alias from ref_book_value v \n" +
            "                        join ref_book_record r on r.id = v.record_id\n" +
            "                        join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')\n" +
            "                        where r.ref_book_id = 8)\n" +
            "                      pivot\n" +
            "                      (\n" +
            "                        max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)\n" +
            "                      )) t\n" +
            "            join (\n" +
            "                 select level i\n" +
            "                 from dual\n" +
            "                 connect by level <= 12\n" +
            "            ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2 \n" +
            "           ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and perversion.lvl = case when (tft.MONTHLY=1 and ft.MONTHLY=0) then perversion.lvl else 1 end\n" +
            "      --отбираем экземпляры с учетом периода сравнения, признака нарастающего истога, списка месяцов     \n" +
            "      left join form_data tfd on (tfd.kind = tfk.id and tfd.FORM_TEMPLATE_ID = tft.id and tfd.DEPARTMENT_REPORT_PERIOD_ID = tdrp.id\n" +
            "        and (tft.COMPARATIVE = 0 or ft.COMPARATIVE = 0 or tfd.COMPARATIVE_DEP_REP_PER_ID = fd.COMPARATIVE_DEP_REP_PER_ID) and (tft.ACCRUING = 0 or ft.ACCRUING = 0 or tfd.ACCRUING = fd.ACCRUING)) \n" +
            "        and coalesce(tfd.PERIOD_ORDER, perversion.month) = perversion.month \n" +
            "      left join form_data_performer fdp on fdp.form_data_id = tfd.id \n" +
            "      left join department fdpd on fdpd.id = fdp.PRINT_DEPARTMENT_ID\n" +
            "      left join department_report_period tcdrp on tcdrp.id = tfd.COMPARATIVE_DEP_REP_PER_ID\n" +
            "      left join report_period tcrp on tcrp.id = tcdrp.REPORT_PERIOD_ID\n" +
            "      left join tax_period tctp on tctp.id = tcrp.TAX_PERIOD_ID\n" +
            "      where fd.id = :sourceFormDataId\n" +
            "  ),\n" +
            "  aggregated_insanity as (\n" +
            "      select departmentId, formtypeid, formdatakind, reportperiodid, month, isExemplarExistent, last_correction_date, global_last_correction_date from                 \n" +
            "        (select case when i.id is null then '0' else '1' end as agg_type, departmentId, formtypeid, formdatakind, reportperiodid, month, max(correction_date) over(partition by departmentId, formtypeid, formdatakind, reportperiodid, month, case when i.id is null then 0 else 1 end) as last_correction_date, case when count(i.id) over(partition by departmentId, formtypeid, formdatakind, reportperiodid, month) > 0 then 1 else 0 end isExemplarExistent\n" +
            "         from insanity i)  \n" +
            "      --транспонирование и агрегирование среди множеств отдельно с сушествующими и несуществующими экземлярами \n" +
            "      pivot \n" +
            "      (\n" +
            "          max(last_correction_date) for agg_type in ('1' as last_correction_date, '0' global_last_correction_date)\n" +
            "      )\n" +
            ")         \n" +
            "select i.id, i.departmentId, i.departmentName, i.correction_date, ai.last_correction_date, ai.global_last_correction_date, i.reportperiodid, i.departmentReportPeriod, i.periodName, i.year, i.state, i.templateState, i.formTypeId, i.formTypeName, i.formDataKind, i.performerId, i.performerName, i.compPeriodId, i.compPeriodName, i.compPeriodYear, i.compPeriodStartDate, i.accruing, i.month, i.manual \n" +
            "       from insanity i\n" +
            "       --обращение к аггрегированным данным для определения, какие существуют данные в связке по подразделению, типу, виду, периоду и месяцу экземпляры данных, их максимальную дату и дату последнего периода корректировки, если данные по экземлярам отсутствуют \n" +
            "       left join aggregated_insanity ai on i.id is null and ai.departmentId = i.departmentId and ai.formtypeid = i.formtypeid and ai.formdatakind = i.formdatakind and ai.reportperiodid = i.reportperiodid  and nvl(i.month,-1) = nvl(ai.month,-1)\n" +
            "       --отбираем либо записи, либо где идентификатор формы существует, либо если не существует, то берем запись с максимально доступной датой корректировки \n" +
            "       where (id is not null or (ai.isExemplarExistent = 0 and nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(ai.global_last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY'))))\n" +
            "       and (:excludeIfNotExist != 1 or id is not null) and (id is null or :stateRestriction is null or state = :stateRestriction)\n" +
            "       order by formTypeName, state, departmentName, id";

    @Override
    public List<Relation> getDestinationsInfo(long sourceFormDataId, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceFormDataId", sourceFormDataId);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            return getNamedParameterJdbcTemplate().query(GET_DESTINATIONS_INFO, params, new RowMapper<Relation>() {
                @Override
                public Relation mapRow(ResultSet rs, int i) throws SQLException {
                    Relation relation =  mapFormCommon(rs, light, false);
                    relation.setSource(false);
                    return relation;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    private static final String GET_DECLARATION_DESTINATIONS_INFO = "with insanity as \n" +
            "     (\n" +
            "     select tdd.id, td.id as departmentId, td.name as departmentName, tdrp.id as departmentReportPeriod, ttp.YEAR, trp.id as reportperiodid, trp.name as periodName, \n" +
            "     tdrp.CORRECTION_DATE, tdd.IS_ACCEPTED, tdt.status as templateState, dt.id as declarationTypeId, dt.name as declarationTypeName, tdd.TAX_ORGAN_CODE as taxOrgan, tdd.kpp \n" +
            "      from (\n" +
            "           select neighbours_fd.id, \n" +
            "                  neighbours_fd.form_template_id,\n" +
            "                  neighbours_fd.kind, \n" +
            "                  neighbours_drp.department_id, \n" +
            "                  neighbours_drp.correction_date, \n" +
            "                  neighbours_drp.report_period_id,\n" +
            "                  coalesce(lag(neighbours_drp.correction_date) over (partition by neighbours_drp.department_id order by neighbours_drp.correction_date desc nulls last), to_date('31.12.9999', 'DD.MM.YYYY')) as next_correction_date\n" +
            "            from form_data fd \n" +
            "            join department_report_period drp on drp.id = fd.DEPARTMENT_REPORT_PERIOD_ID and fd.id = :sourceFormDataId\n" +
            "            join department_report_period neighbours_drp on neighbours_drp.report_period_id = drp.report_period_id      \n" +
            "            join form_data neighbours_fd on neighbours_fd.department_report_period_id = neighbours_drp.id and neighbours_fd.form_template_id = fd.form_template_id and neighbours_fd.kind = fd.kind     \n" +
            "            ) fd             \n" +
            "      join report_period rp on rp.id = fd.REPORT_PERIOD_ID\n" +
            "      join form_template ft on ft.id = fd.FORM_TEMPLATE_ID\n" +
            "      join department_form_type dft on (dft.DEPARTMENT_ID = fd.DEPARTMENT_ID and dft.kind = fd.KIND and dft.FORM_TYPE_ID = ft.TYPE_ID)\n" +
            "      --ограничиваем назначения по пересечению с периодом приемника\n" +
            "      join declaration_source ds on (ds.SRC_DEPARTMENT_FORM_TYPE_ID = dft.id and ((ds.period_end >= rp.CALENDAR_START_DATE or ds.period_end is null) and ds.period_start <= rp.END_DATE))\n" +
            "      join department_declaration_type tddt on tddt.id = ds.DEPARTMENT_DECLARATION_TYPE_ID\n" +
            "      join declaration_type dt on dt.id = tddt.DECLARATION_TYPE_ID\n" +
            "      --отбираем приемники у которых дата корректировки попадает в период действия даты корректировки от текущего источника и до следующего (с большей датой корректировки)\n" +
            "      join department_report_period tdrp on (tdrp.DEPARTMENT_ID = tddt.DEPARTMENT_ID and tdrp.REPORT_PERIOD_ID = fd.REPORT_PERIOD_ID and nvl(tdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) between nvl(fd.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) and fd.NEXT_CORRECTION_DATE - 1)\n" +
            "      join department td on td.id = tdrp.DEPARTMENT_ID\n" +
            "      join report_period trp on trp.id = tdrp.REPORT_PERIOD_ID\n" +
            "      join tax_period ttp on ttp.ID = trp.TAX_PERIOD_ID\n" +
            "      --отбираем макет действующий для приемника в периоде источника\n" +
            "      join declaration_template tdt on (tdt.DECLARATION_TYPE_ID = dt.ID and tdt.status in (0,1) and tdt.version = (select max(dt2.version) from declaration_template dt2 where dt2.DECLARATION_TYPE_ID = tdt.DECLARATION_TYPE_ID and extract(year from dt2.version) <= ttp.year)) \n" +
            "      --отбираем экземпляры с учетом периода сравнения, признака нарастающего истога, списка месяцов     \n" +
            "      left join declaration_data tdd on (tdd.DECLARATION_TEMPLATE_ID = tdt.id and tdd.DEPARTMENT_REPORT_PERIOD_ID = tdrp.id)\n" +
            "      where fd.id = :sourceFormDataId\n" +
            "  ),         \n" +
            "  aggregated_insanity as (\n" +
            "      select departmentId, declarationTypeId, reportperiodid, isExemplarExistent, last_correction_date, global_last_correction_date from                 \n" +
            "        (select case when i.id is null then '0' else '1' end as agg_type, departmentId, declarationTypeId, reportperiodid, max(correction_date) over(partition by departmentId, declarationTypeId, reportperiodid, case when i.id is null then 0 else 1 end) as last_correction_date, case when count(i.id) over(partition by departmentId, declarationTypeId, reportperiodid) > 0 then 1 else 0 end isExemplarExistent\n" +
            "         from insanity i)  \n" +
            "      --транспонирование и агрегирование среди множеств отдельно с сушествующими и несуществующими экземлярами \n" +
            "      pivot \n" +
            "      (\n" +
            "          max(last_correction_date) for agg_type in ('1' as last_correction_date, '0' global_last_correction_date)\n" +
            "      )\n" +
            ")         \n" +
            "select i.id, i.departmentId, i.departmentName, i.correction_date, ai.last_correction_date, ai.global_last_correction_date, i.reportperiodid, i.departmentReportPeriod, i.periodName, i.year, i.IS_ACCEPTED, i.templateState, i.declarationTypeId, i.declarationTypeName, i.taxOrgan, i.kpp\n" +
            "       from insanity i\n" +
            "       --обращение к аггрегированным данным для определения, какие существуют данные в связке по подразделению, типу, виду, периоду и месяцу экземпляры данных, их максимальную дату и дату последнего периода корректировки, если данные по экземлярам отсутствуют \n" +
            "       left join aggregated_insanity ai on i.id is null and ai.departmentId = i.departmentId and ai.declarationTypeId = i.declarationTypeId and ai.reportperiodid = i.reportperiodid \n" +
            "       --отбираем либо записи, либо где идентификатор формы существует, либо если не существует, то берем запись с максимально доступной датой корректировки \n" +
            "       where (id is not null or (ai.isExemplarExistent = 0 and nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(ai.global_last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY')))) \n" +
            "       and (:excludeIfNotExist != 1 or id is not null) and (id is null or :stateRestriction is null or IS_ACCEPTED = :stateRestriction)\n" +
            "       order by declarationTypeName, IS_ACCEPTED, departmentName, id";

    @Override
    public List<Relation> getDeclarationDestinationsInfo(long sourceFormDataId, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sourceFormDataId", sourceFormDataId);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null && (stateRestriction == WorkflowState.CREATED || stateRestriction ==WorkflowState.ACCEPTED) ?
                stateRestriction == WorkflowState.ACCEPTED ? 1 : 0
                : null);
        try {
            return getNamedParameterJdbcTemplate().query(GET_DECLARATION_DESTINATIONS_INFO, params, new RowMapper<Relation>() {
                @Override
                public Relation mapRow(ResultSet rs, int i) throws SQLException {
                    Relation relation = new Relation();
                    relation.setSource(false);
                    relation.setDeclarationDataId(SqlUtils.getLong(rs, "id"));
                    relation.setCreated(relation.getDeclarationDataId() != null);
                    relation.setState(relation.getDeclarationDataId() == null ? WorkflowState.NOT_EXIST :
                            SqlUtils.getInteger(rs, "IS_ACCEPTED") == 1 ? WorkflowState.ACCEPTED : WorkflowState.CREATED);
                    relation.setStatus(SqlUtils.getInteger(rs, "templateState") == 0);
                    relation.setTaxOrganCode(rs.getString("taxOrgan"));
                    relation.setKpp(rs.getString("kpp"));
                    if (light) {
                        relation.setDepartmentId(SqlUtils.getInteger(rs, "departmentId"));
                        relation.setFullDepartmentName(rs.getString("departmentName"));
                        relation.setCorrectionDate(rs.getDate("correction_date"));
                        relation.setPeriodName(rs.getString("periodName"));
                        relation.setYear(SqlUtils.getInteger(rs, "year"));
                        relation.setDeclarationTypeName(rs.getString("declarationTypeName"));
                    } else {
                        relation.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs, "departmentId")));
                        relation.setDepartmentReportPeriod(departmentReportPeriodDao.get(SqlUtils.getInteger(rs, "departmentReportPeriod")));
                        relation.setDeclarationType(declarationTypeDao.get(SqlUtils.getInteger(rs, "declarationTypeId")));
                    }
                    return relation;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    private static final String GET_DECLARATION_SOURCES_INFO = "with insanity as \n" +
            "     (\n" +
            "     select sfd.id, sd.id as departmentId, sd.name as departmentName, sdrp.id as departmentReportPeriod, stp.YEAR, srp.name as periodName, \n" +
            "     sdrp.CORRECTION_DATE, sfd.state, sft.status as templateState, sfd.manual, \n" +
            "     st.id as formTypeId, st.name as formTypeName, sfk.id as formDataKind, fdpd.id as performerId, fdpd.name as performerName, \n" +
            "     case when sft.MONTHLY=1 then perversion.month else sfd.PERIOD_ORDER end as month, sdft.id as sdft_id\n" +
            "      from declaration_data dd \n" +
            "      join department_report_period drp on drp.id = dd.DEPARTMENT_REPORT_PERIOD_ID and dd.id = :declarationId\n" +
            "      join report_period rp on rp.id = drp.REPORT_PERIOD_ID\n" +
            "      join declaration_template dt on dt.id = dd.DECLARATION_TEMPLATE_ID\n" +
            "      join department_declaration_type ddt on (ddt.DEPARTMENT_ID = drp.DEPARTMENT_ID and ddt.DECLARATION_TYPE_ID = dt.DECLARATION_TYPE_ID)\n" +
            "      --ограничиваем назначения по пересечению с периодом приемника\n" +
            "      join declaration_source ds on (ds.DEPARTMENT_DECLARATION_TYPE_ID = ddt.id and ((ds.period_end >= rp.CALENDAR_START_DATE or ds.period_end is null) and ds.period_start <= rp.END_DATE))\n" +
            "      join department_form_type sdft on sdft.id = ds.SRC_DEPARTMENT_FORM_TYPE_ID\n" +
            "      join form_type st on st.id = sdft.FORM_TYPE_ID\n" +
            "      join form_kind sfk on sfk.ID = sdft.KIND\n" +
            "      --отбираем источники у которых дата корректировки ближе всего\n" +
            "      join department_report_period sdrp on (sdrp.DEPARTMENT_ID = sdft.DEPARTMENT_ID and sdrp.REPORT_PERIOD_ID = drp.REPORT_PERIOD_ID and nvl(sdrp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')) <= nvl(drp.CORRECTION_DATE, to_date('01.01.0001', 'DD.MM.YYYY')))\n" +
            "      join department sd on sd.id = sdrp.DEPARTMENT_ID\n" +
            "      join report_period srp on srp.id = sdrp.REPORT_PERIOD_ID\n" +
            "      join tax_period stp on stp.ID = srp.TAX_PERIOD_ID\n" +
            "      --отбираем макет действующий для приемника в периоде приемника\n" +
            "      join form_template sft on (sft.TYPE_ID = st.ID and sft.status in (0,1) and sft.version = (select max(ft2.version) from form_template ft2 where ft2.TYPE_ID = sft.TYPE_ID and extract(year from ft2.version) <= stp.year))\n" +
            "      --если макет источника ежемесячный, то отбираем все возможные месяца для него из справочника\n" +
            "      left join\n" +
            "           (\n" +
            "           select t.id as record_id, lvl.i as lvl, extract(month from ADD_MONTHS(t.d1, lvl.i - 1)) as month, t.d1, t.d2 from (      \n" +
            "              select id, end_date as d2, calendar_start_date as d1, round(months_between(end_date, calendar_start_date)) as months_between_cnt from (\n" +
            "                        select r.id, v.date_value, a.alias from ref_book_value v \n" +
            "                        join ref_book_record r on r.id = v.record_id\n" +
            "                        join ref_book_attribute a on a.id = v.ATTRIBUTE_ID and a.alias in ('CALENDAR_START_DATE', 'END_DATE')\n" +
            "                        where r.ref_book_id = 8)\n" +
            "                      pivot\n" +
            "                      (\n" +
            "                        max(date_value) for alias in ('END_DATE' END_DATE, 'CALENDAR_START_DATE' CALENDAR_START_DATE)\n" +
            "                      )) t\n" +
            "            join (\n" +
            "                 select level i\n" +
            "                 from dual\n" +
            "                 connect by level <= 12\n" +
            "            ) lvl on ADD_MONTHS(t.d1, lvl.i - 1) <= t.d2 \n" +
            "           ) perversion on perversion.record_id = rp.DICT_TAX_PERIOD_ID and perversion.lvl = case when sft.MONTHLY=1 then perversion.lvl else 1 end\n" +
            "      --отбираем экземпляры с учетом списка месяцов     \n" +
            "      left join form_data sfd on (sfd.kind = sfk.id and sfd.FORM_TEMPLATE_ID = sft.id and sfd.DEPARTMENT_REPORT_PERIOD_ID = sdrp.id) and coalesce(sfd.PERIOD_ORDER, perversion.month) = perversion.month\n" +
            "      left join form_data_performer fdp on fdp.form_data_id = sfd.id \n" +
            "      left join department fdpd on fdpd.id = fdp.PRINT_DEPARTMENT_ID  \n" +
            "           ),\n" +
            "  aggregated_insanity as (\n" +
            "      select sdft_id, max(correction_date) as last_correction_date\n" +
            "      from insanity i\n" +
            "      where id is not null\n" +
            "      group by sdft_id\n" +
            "  )         \n" +
            "select id, departmentId, departmentName, correction_date, departmentReportPeriod, periodName, year, state, " +
            "templateState, formTypeId, formTypeName, formDataKind, performerId, performerName, month, manual \n" +
            "       from insanity i\n" +
            "       left join aggregated_insanity i_agg on i.sdft_id = i_agg.sdft_id \n" +
            "       where nvl(i.correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) = nvl(i_agg.last_correction_date, to_date('01.01.0001', 'DD.MM.YYYY')) and (:excludeIfNotExist != 1 or id is not null) and (id is null or :stateRestriction is null or state = :stateRestriction)\n" +
            "       order by formTypeName, state, departmentName, id";

    @Override
    public List<Relation> getDeclarationSourcesInfo(long declarationId, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("declarationId", declarationId);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            return getNamedParameterJdbcTemplate().query(GET_DECLARATION_SOURCES_INFO, params, new RowMapper<Relation>() {
                @Override
                public Relation mapRow(ResultSet rs, int i) throws SQLException {
                    Relation relation =  mapFormCommon(rs, light, true);
                    relation.setSource(true);
                    return relation;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    private Relation mapFormCommon(ResultSet rs, boolean light, boolean forDeclaration) throws SQLException {
        Relation relation = new Relation();
        relation.setFormDataId(SqlUtils.getLong(rs, "id"));
        relation.setCreated(relation.getFormDataId() != null);
        relation.setState(WorkflowState.fromId(SqlUtils.getInteger(rs, "state")));
        relation.setStatus(SqlUtils.getInteger(rs, "templateState") == 0);
        relation.setFormDataKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "formDataKind")));
        relation.setManual(rs.getBoolean("manual"));
        if (!forDeclaration) {
            relation.setAccruing(rs.getBoolean("accruing"));
        }
        relation.setMonth(SqlUtils.getInteger(rs, "month"));
        if (light) {
            relation.setDepartmentId(SqlUtils.getInteger(rs, "departmentId"));
            relation.setFullDepartmentName(rs.getString("departmentName"));
            relation.setCorrectionDate(rs.getDate("correction_date"));
            relation.setPeriodName(rs.getString("periodName"));
            relation.setYear(SqlUtils.getInteger(rs, "year"));
            relation.setFormTypeName(rs.getString("formTypeName"));
            if (!forDeclaration) {
                relation.setComparativePeriodYear(SqlUtils.getInteger(rs, "compPeriodYear"));
                relation.setComparativePeriodStartDate(rs.getDate("compPeriodStartDate"));
                String baseCompPeriodName = rs.getString("compPeriodName");
                relation.setComparativePeriodName(relation.isAccruing() ?
                        FormatUtils.getAccName(baseCompPeriodName, relation.getComparativePeriodStartDate()) : baseCompPeriodName);
            }
            relation.setPerformerName(rs.getString("performerName"));
        } else {
            relation.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs, "departmentId")));
            relation.setDepartmentReportPeriod(departmentReportPeriodDao.get(SqlUtils.getInteger(rs, "departmentReportPeriod")));
            Integer performerId = SqlUtils.getInteger(rs, "performerId");
            if (performerId != null) {
                relation.setPerformer(departmentDao.getDepartment(SqlUtils.getInteger(rs, "performerId")));
            }
            if (!forDeclaration) {
                Integer comparativePeriodId  = SqlUtils.getInteger(rs, "compPeriodId");
                if (comparativePeriodId != null) {
                    relation.setComparativePeriod(departmentReportPeriodDao.get(comparativePeriodId));
                }
            }
            relation.setFormType(formTypeDao.get(SqlUtils.getInteger(rs, "formTypeId")));
        }
        return relation;
    }
}
