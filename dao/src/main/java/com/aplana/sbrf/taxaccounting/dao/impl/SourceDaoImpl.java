package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.SourceDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.source.SourceObject;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public class SourceDaoImpl extends AbstractDao implements SourceDao {

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

    private static final String ADD_DECLARATION_CONSOLIDATION =
            "insert into DECLARATION_DATA_CONSOLIDATION (TARGET_DECLARATION_DATA_ID, SOURCE_DECLARATION_DATA_ID) values (?,?)";

    @Override
    public void addDeclarationConsolidationInfo(final Long tgtDeclarationId, Collection<Long> srcFormDataIds) {
        final Object[] srcArray = srcFormDataIds.toArray();
        try {
            getJdbcTemplate().batchUpdate(ADD_DECLARATION_CONSOLIDATION, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, tgtDeclarationId);
                    ps.setLong(2, (Long) srcArray[i]);
                }

                @Override
                public int getBatchSize() {
                    return srcArray.length;
                }
            });
        } catch (DataAccessException e) {
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

    @Override
    public int updateDDConsolidationInfo(long sourceFormId) {
        return getJdbcTemplate().update(
                "update DECLARATION_DATA_CONSOLIDATION set SOURCE_DECLARATION_DATA_ID = null where SOURCE_DECLARATION_DATA_ID = ?",
                sourceFormId);
    }

    @Override
    public boolean isDDConsolidationTopical(long ddTargetId) {
        return getJdbcTemplate().queryForObject(
                "select count(*) from DECLARATION_DATA_CONSOLIDATION where TARGET_DECLARATION_DATA_ID = ? and SOURCE_DECLARATION_DATA_ID is null",
                Integer.class, ddTargetId) == 0;
    }

    @Override
    public List<Relation> getSourcesInfo(long targetId) {
        String sql = "select ddc.source_declaration_data_id as id, dep.NAME as departmentName, drp.correction_date, dt.form_kind, dt.NAME as declaration_type_name, tp.YEAR, tp.tax_type, rpt.NAME as periodName, dd.STATE\n" +
                "from declaration_data_consolidation ddc \n" +
                "left join declaration_data dd on dd.id = ddc.source_declaration_data_id\n" +
                "left join department_report_period drp on drp.id = dd.department_report_period_id\n" +
                "left join department dep on dep.id = drp.department_id\n" +
                "left join declaration_template dt on dt.ID = dd.declaration_template_id\n" +
                "left join report_period rp on rp.id = drp.report_period_id\n" +
                "left join tax_period tp on tp.id = rp.tax_period_id\n" +
                "left join report_period_type rpt on rpt.id = rp.dict_tax_period_id\n" +
                "where ddc.target_declaration_data_id = :targetId and ddc.source_declaration_data_id is not null";
        MapSqlParameterSource params = new MapSqlParameterSource("targetId", targetId);
        List<Relation> result = new ArrayList<>();
        getNamedParameterJdbcTemplate().query(sql, params, new CommonSourcesCallBackHandler(result, true));
        return result;
    }


    public List<Relation> getDestinationsInfo(long sourceId) {
        String sql = "select ddc.target_declaration_data_id as id, dep.NAME as departmentName, drp.correction_date, dt.form_kind, dt.NAME as declaration_type_name, tp.YEAR, tp.tax_type, rpt.NAME as periodName, dd.STATE\n" +
                "from declaration_data_consolidation ddc \n" +
                "left join declaration_data dd on dd.id = ddc.target_declaration_data_id\n" +
                "left join department_report_period drp on drp.id = dd.department_report_period_id\n" +
                "left join department dep on dep.id = drp.department_id\n" +
                "left join declaration_template dt on dt.ID = dd.declaration_template_id\n" +
                "left join report_period rp on rp.id = drp.report_period_id\n" +
                "left join tax_period tp on tp.id = rp.tax_period_id\n" +
                "left join report_period_type rpt on rpt.id = rp.dict_tax_period_id\n" +
                "where ddc.source_declaration_data_id = :sourceId and ddc.target_declaration_data_id is not null";
        MapSqlParameterSource params = new MapSqlParameterSource("sourceId", sourceId);
        List<Relation> result = new ArrayList<>();
        getNamedParameterJdbcTemplate().query(sql, params, new CommonSourcesCallBackHandler(result, false));
        return result;
    }

    private class CommonSourcesCallBackHandler implements RowCallbackHandler {
        private List<Relation> result;
        private boolean source;

        public CommonSourcesCallBackHandler(List<Relation> result, boolean source) {
            this.result = result;
            this.source = source;
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            Relation relation = new Relation();
            relation.setSource(source);
            relation.setDeclarationDataId(SqlUtils.getLong(rs, "id"));
            relation.setTaxType(TaxType.fromCode(rs.getString("tax_type").charAt(0))); //
            relation.setFullDepartmentName(rs.getString("departmentName")); //
            relation.setCorrectionDate(rs.getDate("correction_date")); //
            relation.setYear(SqlUtils.getInteger(rs, "year"));
            relation.setDeclarationTypeName(rs.getString("declaration_type_name"));
            relation.setPeriodName(rs.getString("periodName"));
            DeclarationTemplate declarationTemplate = new DeclarationTemplate();
            DeclarationFormKind declarationFormKind = DeclarationFormKind.fromId(SqlUtils.getLong(rs, "form_kind"));
            declarationTemplate.setDeclarationFormKind(declarationFormKind);
            relation.setDeclarationTemplate(declarationTemplate);
            relation.setDeclarationState(State.fromId(rs.getInt("state")));
            result.add(relation);
        }
    }
}
