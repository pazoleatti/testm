package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.source.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;
import java.util.Date;

@Repository
public class SourceDaoImpl extends AbstractDao implements SourceDao {

    private Map<String, Long> buildParametrizedInQuery(String query, List<SourcePair> sourcePairs, PreparedStatementData ps) {
        StringBuilder in = new StringBuilder();
        Map<String, Long> params = new HashMap<String, Long>();
        int paramNum = 0;
        in.append("(");
        for (Iterator<SourcePair> it = sourcePairs.iterator(); it.hasNext();) {
            SourcePair pair = it.next();
            ps.addParam(pair.getSource());
            ps.addParam(pair.getDestination());
            String leftPart = "leftPart" +  paramNum;
            String rightPart = "rightPart" + paramNum;
            in.append("(:").append(leftPart).append(",:").append(rightPart).append(")");
            params.put(leftPart, pair.getSource());
            params.put(rightPart, pair.getDestination());
            if (it.hasNext()) {
                in.append(",");
            }
            paramNum += 1;
        }
        in.append(")");
        ps.appendQuery(String.format(query, in));
        return params;
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
            "where\n" +
            "(src_department_form_type_id, department_form_type_id) in %s --список пар \n" +
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
            "where\n" +
            "(src_department_form_type_id, department_declaration_type_id) in %s --список пар\n" +
            "and (period_end >= :periodStart or period_end is null) --дата открытия периода\n" +
            "and (:periodEnd is null or period_start <= :periodEnd) --дата окончания периода (может быть передана null)\n" +
            "and (:excludedPeriodStart is null or (period_start, period_end) not in ((:excludedPeriodStart, :excludedPeriodEnd))) --исключить этот период";

    @Override
    public Map<SourcePair, List<SourceObject>> getIntersections(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd,
                                                                Date excludedPeriodStart, Date excludedPeriodEnd, final boolean declaration) {
        final Map<SourcePair, List<SourceObject>> result = new HashMap<SourcePair, List<SourceObject>>();
        PreparedStatementData ps = new PreparedStatementData();
        Map<String, Long> pairParams;
        //формируем in-часть вида ((1, 2), (1, 3))
        if (declaration) {
            pairParams = buildParametrizedInQuery(GET_DECLARATION_INTERSECTIONS, sourcePairs, ps);
        } else {
            pairParams = buildParametrizedInQuery(GET_FORM_INTERSECTIONS, sourcePairs, ps);
        }
        //составляем все параметры вместе. PreparedStatementData использовать не получается, т.к несколько параметров могут быть равны null
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        params.put("excludedPeriodStart", excludedPeriodStart);
        params.put("excludedPeriodEnd", excludedPeriodEnd);
        for (Map.Entry<String, Long> param : pairParams.entrySet()) {
            params.put(param.getKey(), param.getValue());
        }

        getNamedParameterJdbcTemplate().query(ps.getQuery().toString(), params, new RowCallbackHandler() {
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
            "select a.id, b.id, :periodStart, cast(:periodEnd as date), 0 as base from department_form_type a, department_form_type b where (a.id, b.id) in %s\n" +
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
        PreparedStatementData ps = new PreparedStatementData();
        //формируем in-часть вида ((1, 2), (1, 3))
        Map<String, Long> pairParams = buildParametrizedInQuery(GET_LOOPS, sourcePairs, ps);
        //составляем все параметры вместе. PreparedStatementData использовать не получается, т.к несколько параметров могут быть равны null
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("periodStart", periodStart);
        params.put("periodEnd", periodEnd);
        for (Map.Entry<String, Long> param : pairParams.entrySet()) {
            params.put(param.getKey(), param.getValue());
        }

        getNamedParameterJdbcTemplate().query(ps.getQuery().toString(), params, new RowCallbackHandler() {
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

    private class SourceBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
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

    @Override
    public List<FormDataInfo> findForms(Date periodStart, Date periodEnd, List<Long> departmentFormTypes) {
        //TODO: Нужен был для проверки существования экземпляров в источниках-приемниках. Оставил пока для компилляции
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DeclarationDataInfo> findDeclarations(Date periodStart, Date periodEnd, List<Long> destinationIds) {
        //TODO: Нужен был для проверки существования экземпляров в источниках-приемниках. Оставил пока для компилляции
        throw new UnsupportedOperationException();
    }
}
