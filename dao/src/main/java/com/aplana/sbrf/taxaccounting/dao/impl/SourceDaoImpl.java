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

    private final static String GET_FORM_INTERSECTIONS = "select distinct a.src_department_form_type_id as source, s_kind.name as s_kind, s_type.name as s_type, s_d.name as s_department, \n" +
            "a.department_form_type_id as destination, d_kind.name as d_kind, d_type.name as d_type, d_d.name as d_department, \n" +
            "a.period_start, a.period_end \n" +
            "from form_data_source a \n" +
            "join department_form_type s_dft on s_dft.id = src_department_form_type_id\n" +
            "join department s_d on s_d.id = s_dft.department_id\n" +
            "join form_kind s_kind on s_kind.id = s_dft.kind\n" +
            "join form_type s_type on s_type.id = s_dft.form_type_id\n" +
            "join department_form_type d_dft on d_dft.id = department_form_type_id\n" +
            "join department d_d on d_d.id = d_dft.department_id\n" +
            "join form_kind d_kind on d_kind.id = d_dft.kind\n" +
            "join form_type d_type on d_type.id = d_dft.form_type_id\n" +
            "where \n" +
            "%s \n" + //--список пар
            "and (period_end >= :periodStart or period_end is null)  \n" + //--дата открытия периода
            "and (:periodEnd is null or period_start <= :periodEnd) \n" + //--дата окончания периода (может быть передана null)
            "and (:excludedPeriodStart is null or (period_start, period_end) not in ((:excludedPeriodStart, :excludedPeriodEnd))) "; //--исключить этот период

    private final static String GET_DECLARATION_INTERSECTIONS = "select distinct a.src_department_form_type_id as source, s_kind.name as s_kind, s_type.name as s_type, s_d.name as s_department, \n" +
            "a.department_declaration_type_id as destination, null as d_kind, d_type.name as d_type, d_d.name as d_department, \n" +
            "a.period_start, a.period_end\n" +
            "from declaration_source a\n" +
            "join department_form_type s_dft on s_dft.id = src_department_form_type_id\n" +
            "join department s_d on s_d.id = s_dft.department_id\n" +
            "join form_kind s_kind on s_kind.id = s_dft.kind\n" +
            "join form_type s_type on s_type.id = s_dft.form_type_id\n" +
            "join department_declaration_type d_ddt on d_ddt.id = department_declaration_type_id\n" +
            "join department d_d on d_d.id = d_ddt.department_id\n" +
            "join declaration_type d_type on d_type.id = d_ddt.declaration_type_id\n" +
            "where \n" +
            "%s \n" + //--список пар
            "and (period_end >= :periodStart or period_end is null) \n" + //--дата открытия периода
            "and (:periodEnd is null or period_start <= :periodEnd) \n" + //--дата окончания периода (может быть передана null)
            "and (:excludedPeriodStart is null or (period_start, period_end) not in ((:excludedPeriodStart, :excludedPeriodEnd))) "; //--исключить этот период

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
                pair.setSourceDepartmentName(rs.getString("s_department"));
                pair.setDestinationDepartmentName(rs.getString("d_department"));

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
            //"--существующие записи из таблицы соответствия\n" +
            "select tds.src_department_form_type_id, tds.department_form_type_id, period_start, period_end, 1 as base from form_data_source tds\n" +
            "union all \n" + //--записи через cross join для возможности фильтрации попарно (можно заменить за запрос из временной таблицы), фиктивные даты = даты для фильтрации
            "select a.id, b.id, :periodStart, cast(:periodEnd as date), 0 as base from department_form_type a, department_form_type b where %s \n" + //--список пар
            ")\n" +
            "select \n" +
            "       CONNECT_BY_ROOT src as IN_src_department_form_type_id, \n" + //--исходный источник
            "       CONNECT_BY_ROOT tgt as IN_department_form_type_id, \n" + //--исходный приемник
            "       src as src_department_form_type_id, \n" +
            "       tgt as department_form_type_id\n" +
            "from subset \n" +
            "where connect_by_iscycle = 1 \n" + // -- есть зацикливание
            "connect by nocycle prior src = tgt and (period_end >= :periodStart or period_end is null) and (:periodEnd is null or period_start <= :periodEnd) \n" + //-- предыдущий источник = текущему приемнику, т.е. поднимаемся наверх
            "start with base  = 0 "; //-- начать с тех записей, которые были добавлены в граф фиктивно

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

    private static final String GET_SOURCE_NAMES = "select dft.id, fk.name as form_kind, ft.name as form_type, d.name as department_name from department_form_type dft\n" +
            "join form_kind fk on fk.id = dft.kind\n" +
            "join form_type ft on ft.id = dft.form_type_id\n" +
            "join department d on ft.id = dft.department_id\n" +
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
                            rs.getString("department_name") + ", " + rs.getString("form_kind") + " - " + rs.getString("form_type")
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
            "   FROM form_data_src \n" + //-- дата начала нового периода
            "   UNION ALL \n" +
            "   SELECT max(coalesce(period_end, to_date('30.12.9999', 'DD.MM.YYYY'))) + interval '1' DAY AS period_start, to_date(:newPeriodEnd, 'DD.MM.YYYY') period_end FROM form_data_src\n" + //-- дата окончания нового периода
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
            "SELECT tfd.id as form_data_id, fd.id as SOURCE_DECLARATION_DATA_ID, tfd.kind AS kind, tfmt.name AS type, td.name AS department, trp.name AS period, ttp.year AS year, tfd.period_order AS month, tdrp.correction_date AS correctionDate, tfd.COMPARATIVE_DEP_REP_PER_ID as drpCompareId \n" +
                    "FROM department_form_type dft\n" +
                    "JOIN form_template ft ON ft.type_id = dft.form_type_id\n" +
                    "JOIN department_report_period drp ON drp.department_id = dft.department_id\n" +
                    "JOIN form_data fd ON (fd.kind = dft.kind AND fd.form_template_id = ft.id AND fd.department_report_period_id = drp.id)\n" +
                    "JOIN form_data_consolidation fdc ON fdc.SOURCE_DECLARATION_DATA_ID = fd.id\n" +
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
            "select tdd.id as declaration_id, fd.id as SOURCE_DECLARATION_DATA_ID, dt.name as type, td.name as department, trp.name as period, ttp.year as year, tdrp.correction_date as correctionDate, tdd.tax_organ_code as taxOrganCode, tdd.kpp as kpp \n" +
            "from department_form_type dft\n" +
            "join form_template ft on ft.type_id = dft.form_type_id\n" +
            "join department_report_period drp on drp.department_id = dft.department_id\n" +
            "join form_data fd on (fd.kind = dft.kind and fd.form_template_id = ft.id and fd.department_report_period_id = drp.id)\n" +
            "join declaration_data_consolidation ddc on ddc.SOURCE_DECLARATION_DATA_ID = fd.id\n" +
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
                    declaration.setSourceId(rs.getLong("SOURCE_DECLARATION_DATA_ID"));
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
                    form.setSourceId(rs.getLong("SOURCE_DECLARATION_DATA_ID"));
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
            "insert into DECLARATION_DATA_CONSOLIDATION (TARGET_DECLARATION_DATA_ID, SOURCE_DECLARATION_DATA_ID) values (?,?)";

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
                    "select 1 from DECLARATION_DATA_CONSOLIDATION where target_declaration_data_id = ? and SOURCE_DECLARATION_DATA_ID = ?",
                    Integer.class,
                    declarationId, sourceFormDataId);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }

        return true;
    }

    private static final String ADD_CONSOLIDATION =
            "insert into FORM_DATA_CONSOLIDATION (TARGET_FORM_DATA_ID, SOURCE_DECLARATION_DATA_ID) values (?,?)";
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
                    "select 1 from FORM_DATA_CONSOLIDATION where TARGET_FORM_DATA_ID = ? and SOURCE_DECLARATION_DATA_ID = ?",
                    Integer.class,
                    formDataId, sourceFormDataId) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public void updateFDConsolidationInfo(long sourceFormId) {
        getJdbcTemplate().update(
                "update FORM_DATA_CONSOLIDATION set SOURCE_DECLARATION_DATA_ID = null where SOURCE_DECLARATION_DATA_ID = ?",
                sourceFormId);
    }

    @Override
    public void updateConsolidationInfo(Set<ConsolidatedInstance> instances, boolean declaration) {
        String sql;
        if (declaration) {
            sql = "update DECLARATION_DATA_CONSOLIDATION set SOURCE_DECLARATION_DATA_ID = null where " + buildConsolidatedPairsInQuery("(SOURCE_DECLARATION_DATA_ID, target_declaration_data_id)", instances);
            getJdbcTemplate().update(sql);
        } else {
            sql = "update FORM_DATA_CONSOLIDATION set SOURCE_DECLARATION_DATA_ID = null where " + buildConsolidatedPairsInQuery("(SOURCE_DECLARATION_DATA_ID, target_form_data_id)", instances);
        }
        getJdbcTemplate().update(sql);
    }

    @Override
    public int updateDDConsolidationInfo(long sourceFormId) {
        return getJdbcTemplate().update(
                "update DECLARATION_DATA_CONSOLIDATION set SOURCE_DECLARATION_DATA_ID = null where SOURCE_DECLARATION_DATA_ID = ?",
                sourceFormId);
    }

    @Override
    public boolean isFDConsolidationTopical(long fdTargetId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from form_data_consolidation where TARGET_FORM_DATA_ID = ? and SOURCE_DECLARATION_DATA_ID is null",
                Integer.class, fdTargetId) == 0;
    }

    @Override
    public boolean isDDConsolidationTopical(long ddTargetId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from DECLARATION_DATA_CONSOLIDATION where TARGET_DECLARATION_DATA_ID = ? and SOURCE_DECLARATION_DATA_ID is null",
                Integer.class, ddTargetId) == 0;
    }

    @Override
    public List<Relation> getSourcesInfo(FormData destinationFormData, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql = "select * from table(form_data_pckg.get_sources(:stateRestriction, :excludeIfNotExist, :destinationFormDataId, :formTemplateId, :departmentReportPeriodId, :kind, :compPeriod, :accruing, :periodOrder))";
        params.put("destinationFormDataId", destinationFormData.getId());
        params.put("formTemplateId", destinationFormData.getId() == null ? destinationFormData.getFormTemplateId() : null);
        params.put("departmentReportPeriodId", destinationFormData.getId() == null ? destinationFormData.getDepartmentReportPeriodId() : null);
        params.put("kind", destinationFormData.getKind() != null ? destinationFormData.getKind().getId() : null);
        params.put("compPeriod", destinationFormData.getId() == null ? destinationFormData.getComparativePeriodId() : null);
        params.put("accruing", destinationFormData.getId() == null ? destinationFormData.isAccruing() ? 1 : 0 : null);
        params.put("periodOrder", destinationFormData.getId() == null ? destinationFormData.getPeriodOrder() : null);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            List<Relation> result = new ArrayList<Relation>();
            getNamedParameterJdbcTemplate().query(sql, params, new CommonSourcesCallBackHandler(result, light, false, true));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    @Override
    public List<Relation> getDestinationsInfo(FormData sourceFormData, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql = "select * from table(form_data_pckg.get_destinations(:stateRestriction, :excludeIfNotExist, :sourceFormDataId, :formTemplateId, :departmentReportPeriodId, :kind, :compPeriod, :accruing, :periodOrder))";
        params.put("sourceFormDataId", sourceFormData.getId());
        params.put("formTemplateId", sourceFormData.getId() == null ? sourceFormData.getFormTemplateId() : null);
        params.put("departmentReportPeriodId", sourceFormData.getId() == null ? sourceFormData.getDepartmentReportPeriodId() : null);
        params.put("kind", sourceFormData.getKind() != null ? sourceFormData.getKind().getId() : null);
        params.put("compPeriod", sourceFormData.getId() == null ? sourceFormData.getComparativePeriodId() : null);
        params.put("accruing", sourceFormData.getId() == null ? sourceFormData.isAccruing() ? 1 : 0 : null);
        params.put("periodOrder", sourceFormData.getId() == null ? sourceFormData.getPeriodOrder() : null);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            List<Relation> result = new ArrayList<Relation>();
            getNamedParameterJdbcTemplate().query(sql, params, new CommonSourcesCallBackHandler(result, light, false, false));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    @Override
    public List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql = "select * from table(declaration_pckg.get_destinations(:stateRestriction, :excludeIfNotExist, :sourceFormDataId, :formTemplateId, :departmentReportPeriodId, :kind, :compPeriod, :accruing))";
        params.put("sourceFormDataId", sourceFormData.getId());
        params.put("formTemplateId", sourceFormData.getId() == null ? sourceFormData.getFormTemplateId() : null);
        params.put("departmentReportPeriodId", sourceFormData.getId() == null ? sourceFormData.getDepartmentReportPeriodId() : null);
        params.put("kind", sourceFormData.getKind() != null ? sourceFormData.getKind().getId() : null);
        params.put("compPeriod", sourceFormData.getId() == null ? sourceFormData.getComparativePeriodId() : null);
        params.put("accruing", sourceFormData.getId() == null ? sourceFormData.isAccruing() ? 1 : 0 : null);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null && (stateRestriction == WorkflowState.CREATED || stateRestriction ==WorkflowState.ACCEPTED) ?
                stateRestriction == WorkflowState.ACCEPTED ? 1 : 0
                : null);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Relation>() {
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
                    relation.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
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
                        //relation.setDeclarationType(declarationTypeDao.get(SqlUtils.getInteger(rs, "declarationTypeId")));
                    }
                    return relation;
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    @Override
    public List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, final boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction) {
        Map<String, Object> params = new HashMap<String, Object>();
        String sql = "select * from table(declaration_pckg.get_sources(:stateRestriction, :excludeIfNotExist, :declarationId, :declarationTemplateId, :departmentReportPeriodId))";
        params.put("declarationId", declaration.getId());
        params.put("departmentReportPeriodId", declaration.getId() == null ? declaration.getDepartmentReportPeriodId() : null);
        params.put("declarationTemplateId", declaration.getId() == null ? declaration.getDeclarationTemplateId() : null);
        params.put("excludeIfNotExist", excludeIfNotExist ? 1 : 0);
        params.put("stateRestriction", stateRestriction != null ? stateRestriction.getId() : null);
        try {
            List<Relation> result = new ArrayList<Relation>();
            //TODO 19.01.2017 Запрос источников создаваемых через форму источники-приемники не используется, отключил вызов из-за ошибок
            //getNamedParameterJdbcTemplate().query(sql, params, new CommonSourcesCallBackHandler(result, light, true, true));
            return result;
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Relation>();
        }
    }

    private void fillPerformers(ResultSet rs, List<Relation> result, Map<Relation, List<String>> map, Map<Relation, List<Department>> mapFull,
                                Relation relation, boolean light
                                ) throws SQLException {
        //Заполняем исполнителей так, потому что их может быть несколько
        if (light) {
            String performerName = rs.getString("performerName");
            if (performerName != null) {
                //Заполняет список исполнителей для назначения
                if (map.containsKey(relation)) {
                    map.get(relation).add(performerName);
                } else {
                    List<String> performerNames = new ArrayList<String>();
                    performerNames.add(performerName);
                    map.put(relation, performerNames);
                    relation.setPerformerNames(performerNames);
                    result.add(relation);
                }
            } else {
                result.add(relation);
            }
        } else {
            Integer performerId = SqlUtils.getInteger(rs, "performerId");
            if (performerId != null) {
                Department performer = departmentDao.getDepartment(SqlUtils.getInteger(rs, "performerId"));
                //Заполняет список исполнителей для назначения
                if (mapFull.containsKey(relation)) {
                    mapFull.get(relation).add(performer);
                } else {
                    List<Department> performers = new ArrayList<Department>();
                    performers.add(performer);
                    mapFull.put(relation, performers);
                    relation.setPerformers(performers);
                    result.add(relation);
                }
            } else {
                result.add(relation);
            }
        }
    }

    private class CommonSourcesCallBackHandler implements RowCallbackHandler {
        private List<Relation> result;
        private boolean light, forDeclaration, source;
        private Map<Relation, List<String>> map = new HashMap<Relation, List<String>>();
        private Map<Relation, List<Department>> mapFull = new HashMap<Relation, List<Department>>();

        public CommonSourcesCallBackHandler(List<Relation> result, boolean light, boolean forDeclaration, boolean source) {
            this.result = result;
            this.light = light;
            this.forDeclaration = forDeclaration;
            this.source = source;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            Relation relation = new Relation();
            relation.setSource(source);
            relation.setFormDataId(SqlUtils.getLong(rs, "id"));
            relation.setCreated(relation.getFormDataId() != null);
            relation.setState(WorkflowState.fromId(SqlUtils.getInteger(rs, "state")));
            relation.setStatus(SqlUtils.getInteger(rs, "templateState") == 0);
            relation.setFormDataKind(FormDataKind.fromId(SqlUtils.getInteger(rs, "formDataKind")));
            relation.setManual(rs.getBoolean("manual"));
            relation.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0)));
            if (!forDeclaration) {
                relation.setAccruing(rs.getBoolean("accruing"));
            }
            relation.setMonth(SqlUtils.getInteger(rs, "month"));
            if (light) {
                relation.setDepartmentId(SqlUtils.getInteger(rs, "departmentId"));
                relation.setFullDepartmentName(rs.getString("departmentName"));
                relation.setCorrectionDate(rs.getDate("correction_date"));
                relation.setYear(SqlUtils.getInteger(rs, "year"));
                relation.setFormTypeName(rs.getString("formTypeName"));
                if (!forDeclaration) {
                    relation.setComparativePeriodYear(SqlUtils.getInteger(rs, "compPeriodYear"));
                    relation.setComparativePeriodStartDate(rs.getDate("compPeriodStartDate"));
                    String basePeriodName = rs.getString("periodName");
                    relation.setPeriodName(relation.isAccruing() ?
                            FormatUtils.getAccName(basePeriodName, rs.getDate("periodStartDate")) : basePeriodName);
                    String baseCompPeriodName = rs.getString("compPeriodName");
                    if (baseCompPeriodName != null) {
                        relation.setComparativePeriodName(relation.isAccruing() ?
                                FormatUtils.getAccName(baseCompPeriodName, relation.getComparativePeriodStartDate()) : baseCompPeriodName);
                    }
                } else {
                    relation.setPeriodName(rs.getString("periodName"));
                }
            } else {
                relation.setDepartment(departmentDao.getDepartment(SqlUtils.getInteger(rs, "departmentId")));
                relation.setDepartmentReportPeriod(departmentReportPeriodDao.get(SqlUtils.getInteger(rs, "departmentReportPeriod")));
                if (!forDeclaration) {
                    Integer comparativePeriodId  = SqlUtils.getInteger(rs, "compPeriodId");
                    if (comparativePeriodId != null) {
                        relation.setComparativePeriod(departmentReportPeriodDao.get(comparativePeriodId));
                    }
                }
                relation.setFormType(formTypeDao.get(SqlUtils.getInteger(rs, "formTypeId")));
            }

            //Заполняем исполнителей так, потому что их может быть несколько
            fillPerformers(rs, result, map, mapFull, relation, light);
        }
    }
}
