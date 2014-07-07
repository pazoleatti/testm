package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.PreparedStatementData;
import com.aplana.sbrf.taxaccounting.model.source.DeclarationDataInfo;
import com.aplana.sbrf.taxaccounting.model.source.FormDataInfo;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class SourceDaoImpl extends AbstractDao implements SourceDao {

    private void buildParametrizedInQuery(String query, List<SourcePair> sourcePairs, PreparedStatementData ps) {
        StringBuilder in = new StringBuilder();
        in.append("(");
        for (Iterator<SourcePair> it = sourcePairs.iterator(); it.hasNext();) {
            SourcePair pair = it.next();
            ps.addParam(pair.getSource());
            ps.addParam(pair.getDestination());
            in.append("(?,?)");
            if (it.hasNext()) {
                in.append(",");
            }
        }
        in.append(")");
        ps.appendQuery(String.format(query, in));
    }

    private final static String GET_FORM_INTERSECTIONS = "select src_department_form_type_id as source, department_form_type_id as destination, period_start, period_end\n" +
            "from form_data_source a\n" +
            "where\n" +
            "(src_department_form_type_id, department_form_type_id) in %s --список пар\n" +
            "and (period_end >= ? or period_end is null) -– дата открытия периода\n" +
            "and period_start <= nvl(?, '31.12.9999')-- дата окончания периода (может быть передана null)";

    private final static String GET_DECLARATION_INTERSECTIONS = "select src_department_form_type_id as source, department_declaration_type_id as destination, period_start, period_end\n" +
            "from declaration_source a\n" +
            "where\n" +
            "(src_department_form_type_id, department_declaration_type_id) in %s --список пар\n" +
            "and (period_end >= ? or period_end is null) -– дата открытия периода\n" +
            "and period_start <= nvl(?, '31.12.9999')-- дата окончания периода (может быть передана null)";

    @Override
    public Map<SourcePair, List<SourceObject>> getIntersections(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd, boolean declaration) {
        final Map<SourcePair, List<SourceObject>> result = new HashMap<SourcePair, List<SourceObject>>();
        PreparedStatementData ps = new PreparedStatementData();
        //формируем in-часть вида ((1, 2), (1, 3))
        if (declaration) {
            buildParametrizedInQuery(GET_DECLARATION_INTERSECTIONS, sourcePairs, ps);
        } else {
            buildParametrizedInQuery(GET_FORM_INTERSECTIONS, sourcePairs, ps);
        }
        ps.addParam(periodStart);
        ps.addParam(periodEnd);

        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                SourcePair pair = new SourcePair(rs.getLong("source"), rs.getLong("destination"));
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
            "union all\n" +
            "--записи через cross join для возможности фильтрации попарно (можно заменить за запрос из временной таблицы), фиктивные даты = даты для фильтрации \n" +
            "select a.id, b.id, ?, ?, 0 as base from department_form_type a, department_form_type b where (a.id, b.id) in %s\n" +
            ")\n" +
            "select \n" +
            "       CONNECT_BY_ROOT src as IN_src_department_form_type_id, --исходный источник\n" +
            "       CONNECT_BY_ROOT tgt as IN_department_form_type_id, --исходный приемник\n" +
            "       src as src_department_form_type_id, \n" +
            "       tgt as department_form_type_id\n" +
            "from subset \n" +
            "where connect_by_iscycle = 1 -- есть зацикливание\n" +
            "connect by nocycle prior src = tgt and (period_end >= ? or period_end is null) and period_start <= nvl(?, '31.12.9999') -- предыдущий источник = текущему приемнику, т.е. поднимаемся наверх\n" +
            "start with base  = 0; -- начать с тех записей, которые были добавлены в граф фиктивно";

    @Override
    public Map<SourcePair, SourcePair> getLoops(List<SourcePair> sourcePairs, Date periodStart, Date periodEnd) {
        final Map<SourcePair, SourcePair> result = new HashMap<SourcePair, SourcePair>();
        PreparedStatementData ps = new PreparedStatementData();
        ps.addParam(periodStart);
        ps.addParam(periodEnd);
        //формируем in-часть вида ((1, 2), (1, 3))
        buildParametrizedInQuery(GET_LOOPS, sourcePairs, ps);
        ps.addParam(periodStart);
        ps.addParam(periodEnd);

        getJdbcTemplate().query(ps.getQuery().toString(), ps.getParams().toArray(), new RowCallbackHandler() {
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

        private SourceBatchPreparedStatementSetter(List<SourceObject> sources) {
            this.sources = sources;
        }

        private SourceBatchPreparedStatementSetter(List<SourceObject> sources, Date periodStart, Date periodEnd) {
            this.sources = sources;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            updateMode = true;
        }

        @Override
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            SourceObject sourceObject = sources.get(i);
            java.sql.Date periodEndSql = sourceObject.getPeriodEnd() != null ? new java.sql.Date(sourceObject.getPeriodEnd().getTime()) : null;
            if (updateMode) {
                ps.setDate(1, new java.sql.Date(periodStart.getTime()));
                ps.setDate(2, new java.sql.Date(periodEnd.getTime()));
                ps.setLong(3, sourceObject.getSourcePair().getSource());
                ps.setLong(4, sourceObject.getSourcePair().getDestination());
                ps.setDate(5, new java.sql.Date(sourceObject.getPeriodStart().getTime()));
                ps.setDate(6, periodEndSql);
                ps.setDate(7, periodEndSql);
            } else {
                ps.setLong(1, sourceObject.getSourcePair().getSource());
                ps.setLong(2, sourceObject.getSourcePair().getDestination());
                ps.setDate(3, new java.sql.Date(sourceObject.getPeriodStart().getTime()));
                ps.setDate(4, periodEndSql);
                ps.setDate(5, periodEndSql);
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
                    new SourceBatchPreparedStatementSetter(sources));
        } else {
            getJdbcTemplate().batchUpdate("delete from form_data_source where src_department_form_type_id = ? and department_form_type_id = ? and period_start = ? and ((? is null and period_end is null) or period_end = ?)",
                    new SourceBatchPreparedStatementSetter(sources));
        }
    }

    @Override
    public void createAll(List<SourceObject> sources, boolean declaration) {
        if (declaration) {
            getJdbcTemplate().batchUpdate("insert into declaration_source (src_department_form_type_id, department_declaration_type_id, period_start, period_end) values (?,?,?,?)",
                    new SourceBatchPreparedStatementSetter(sources));
        } else {
            getJdbcTemplate().batchUpdate("insert into form_data_source (src_department_form_type_id, department_form_type_id, period_start, period_end) values (?,?,?,?)",
                    new SourceBatchPreparedStatementSetter(sources));
        }
    }

    @Override
    public void updateAll(List<SourceObject> sources, Date periodStart, Date periodEnd, boolean declaration) {
        if (declaration) {
            getJdbcTemplate().batchUpdate("update declaration_source set period_start = ?, period_end = ? where src_department_form_type_id = ? and department_declaration_type_id = ? and period_start = ? and period_end = ?",
                    new SourceBatchPreparedStatementSetter(sources, periodStart, periodEnd));
        } else {
            getJdbcTemplate().batchUpdate("update form_data_source set period_start = ?, period_end = ? where src_department_form_type_id = ? and department_form_type_id = ? and period_start = ? and period_end = ?",
                    new SourceBatchPreparedStatementSetter(sources, periodStart, periodEnd));
        }
    }

    private static final String GET_SOURCE_NAMES = "select dft.id, fk.name as form_kind, ft.name as form_type from department_form_type dft\n" +
            "join form_kind fk on fk.id = dft.kind\n" +
            "join form_type ft on ft.id = dft.form_type_id\n" +
            "where dft.id in ";

    @Override
    public Map<Long, String> getSourceNames(List<Long> sourceIds) {
        final Map<Long, String> result = new HashMap<Long, String>();
        try {
            //TODO поправить in часть после мержа
            String sql = GET_SOURCE_NAMES + SqlUtils.transformToSqlInStatement(sourceIds);
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
    public List<Long> checkDepartmentFormTypesExistence(List<Long> departmentFormTypeIds) {
        try {
            //TODO поправить in часть после мержа
            String sql = "select id from department_form_type where id in "+ SqlUtils.transformToSqlInStatement(departmentFormTypeIds);
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
