package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookKnfType;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Реализация Dao для работы с декларациями
 *
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDataDaoImpl extends AbstractDao implements DeclarationDataDao {

    private static final Log LOG = LogFactory.getLog(DeclarationDataDaoImpl.class);
    private final static String FIND_DD_BY_RANGE_IN_RP =
            "select dd.id " +
                    " from DECLARATION_DATA dd \n" +
                    "  INNER JOIN DEPARTMENT_REPORT_PERIOD drp ON dd.DEPARTMENT_REPORT_PERIOD_ID = drp.ID\n" +
                    "  INNER JOIN REPORT_PERIOD rp ON drp.REPORT_PERIOD_ID = rp.ID\n" +
                    "  where dd.DECLARATION_TEMPLATE_ID = :decTemplateId and (rp.CALENDAR_START_DATE NOT BETWEEN :startDate AND :endDate\n" +
                    "    OR rp.END_DATE NOT BETWEEN :startDate AND :endDate)";

    @Override
    public SecuredEntity getSecuredEntity(long id) {
        return get(id);
    }

    @Override
    public DeclarationData get(long declarationDataId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, dd.last_data_modified, " +
                            "dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id AS knf_type_id, knf_type.name AS knf_type_name " +
                            "from declaration_data dd " +
                            "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                            "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                            "where dd.id = ?",
                    new Object[]{declarationDataId},
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(DECLARATION_NOT_FOUND_MESSAGE, declarationDataId);
        }
    }

    @Override
    public List<DeclarationData> get(List<Long> declarationDataIds) {
        try {
            return getJdbcTemplate().query(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, dd.last_data_modified, " +
                            "dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                            "from declaration_data dd " +
                            "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                            "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                            "where " + SqlUtils.transformToSqlInStatement("dd.id", declarationDataIds),
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException("Не удалось удалить налоговые формы");
        }
    }

    @Override
    public List<String> getDeclarationDataKppList(long declarationDataId) {
        return getJdbcTemplate().queryForList("select kpp from declaration_data_kpp where declaration_data_id = ?", String.class, declarationDataId);
    }

    @Override
    public void createDeclarationDataKppList(final long declarationDataId, final Set<String> kppSet) {
        if (kppSet != null && !kppSet.isEmpty()) {
            final Iterator<String> iterator = kppSet.iterator();
            getJdbcTemplate().batchUpdate("insert into declaration_data_kpp(declaration_data_id, kpp) values(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, declarationDataId);
                    ps.setString(2, iterator.next());
                }

                @Override
                public int getBatchSize() {
                    return kppSet.size();
                }
            });
        }
    }

    @Override
    public List<Long> getDeclarationDataPersonIds(long declarationDataId) {
        return getJdbcTemplate().queryForList("select person_id from declaration_data_person where declaration_data_id = ?", Long.class, declarationDataId);
    }

    @Override
    public void createDeclarationDataPersonIds(final long declarationDataId, final Set<Long> personsIds) {
        if (personsIds != null && !personsIds.isEmpty()) {
            final Iterator<Long> iterator = personsIds.iterator();
            getJdbcTemplate().batchUpdate("insert into declaration_data_person(declaration_data_id, person_id) values(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setLong(1, declarationDataId);
                    ps.setLong(2, iterator.next());
                }

                @Override
                public int getBatchSize() {
                    return personsIds.size();
                }
            });
        }
    }

    @Override
    public void delete(long id) {
        int count = getJdbcTemplate().update("delete from declaration_data where id = ?", id);
        if (count == 0) {
            throw new DaoException("Не удалось удалить налоговую форму с id = %d, так как она не существует", id);
        }
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String oktmo, String taxOrganCode, Long asnuId, String fileName) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("departmentReportPeriodId", departmentReportPeriodId);
            params.addValue("declarationTypeId", declarationTypeId);
            params.addValue("kpp", kpp != null ? kpp.toLowerCase() : null);
            params.addValue("oktmo", oktmo != null ? oktmo.toLowerCase() : null);
            params.addValue("taxOrganCode", taxOrganCode != null ? taxOrganCode.toLowerCase() : null);
            params.addValue("asnuId", asnuId);
            params.addValue("fileName", fileName != null ? fileName.toLowerCase() : null);
            return getNamedParameterJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                            "dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                            "from declaration_data dd " +
                            "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                            "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                            "where drp.id = :departmentReportPeriodId " +
                            "and exists (select 1 from declaration_template dt where dd.declaration_template_id=dt.id " +
                            "and dt.declaration_type_id = :declarationTypeId) and (:kpp is null or lower(dd.kpp) = :kpp) and (:oktmo is null or lower(dd.oktmo) = :oktmo) " +
                            "and (:taxOrganCode is null or lower(dd.tax_organ_code) = :taxOrganCode) " +
                            "and (:asnuId is null or asnu_id = :asnuId) and (:fileName is null or lower(file_name) = :fileName)",
                    params, new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new DaoException(
                    "Для заданного сочетания параметров найдено несколько налоговых форм: declarationTypeId = %d, departmentReportPeriodId = %d",
                    declarationTypeId,
                    departmentReportPeriodId
            );
        }
    }

    @Override
    public List<DeclarationData> find(String fileName) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fileName", fileName.toLowerCase());
        return getNamedParameterJdbcTemplate().query(
                "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                        "dd.department_report_periodd, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_cr_ieated, " +
                        "dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                        "from declaration_data dd " +
                        "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                        "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                        "where lower(file_name) = :fileName",
                params, new DeclarationDataRowMapper()
        );
    }

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId) {
        return find(declarationTypeId, null, departmentReportPeriodId, null);
    }

    @Override
    public DeclarationData findConsolidated(RefBookKnfType knfType, int departmentReportPeriodId) {
        List<DeclarationData> declarations = find(DeclarationType.NDFL_CONSOLIDATE, knfType, departmentReportPeriodId, null);
        if (!declarations.isEmpty()) {
            Assert.isTrue(declarations.size() == 1, "Найдено более одной консолидированной формы");
            return declarations.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId, List<Pair<String, String>> kppOktmoPairs) {
        return find(declarationTypeId, null, departmentReportPeriodId, kppOktmoPairs);
    }

    private List<DeclarationData> find(int declarationTypeId, RefBookKnfType knfType, int departmentReportPeriodId, List<Pair<String, String>> kppOktmoPairs) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentReportPeriodId", departmentReportPeriodId);
        params.addValue("declarationTypeId", declarationTypeId);
        String sql = "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                "dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                "from declaration_data dd " +
                "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                "where drp.id = :departmentReportPeriodId\n" +
                "and exists (select 1 from declaration_template dt where dd.declaration_template_id=dt.id and dt.declaration_type_id = :declarationTypeId)";
        if (kppOktmoPairs != null && !kppOktmoPairs.isEmpty()) {
            sql += SqlUtils.pairInStatement(" and (dd.kpp, dd.oktmo)", kppOktmoPairs);
        }
        if (knfType != null) {
            sql += " and dd.knf_type_id = :knfTypeId";
            params.addValue("knfTypeId", knfType.getId());
        }
        return getNamedParameterJdbcTemplate().query(sql, params, new DeclarationDataRowMapper());
    }

    @Override
    public Long getRowNumByFilter(DeclarationDataFilter filter, DeclarationDataSearchOrdering ordering, boolean ascSorting, long declarationDataId) {
        StringBuilder sql = new StringBuilder("select rn from (select dat.*, rownum as rn from (");
        HashMap<String, Object> values = new HashMap<>();
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, values, filter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where declaration_data_id = :declarationDataId");
        values.put("declarationDataId", declarationDataId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(
                    sql.toString(),
                    values,
                    Long.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<DeclarationDataSearchResultItem> findPage(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering,
                                                                  boolean ascSorting, PagingParams pageParams) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        HashMap<String, Object> values = new HashMap<>();
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, values, declarationFilter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where ordDat.rn between :fromIndex and :toIndex")
                .append(" order by ordDat.rn");
        values.put("fromIndex", pageParams.getStartIndex() + 1);
        values.put("toIndex", pageParams.getStartIndex() + pageParams.getCount());
        List<DeclarationDataSearchResultItem> records = getNamedParameterJdbcTemplate().query(
                sql.toString(),
                values,
                new DeclarationDataSearchResultItemMapper()
        );
        return new PagingResult<>(records, getCount(declarationFilter));
    }

    @Override
    public PagingResult<DeclarationDataJournalItem> findPage(DeclarationDataFilter filter, PagingParams pagingParams) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder(
                "select dd.id declarationDataId, dkind.name declarationKind, dtype.name declarationType, dep_fullpath.shortname department,\n" +
                        "   asnu.name asnuName, knf_type.name knfTypeName, state.name state, dd.file_name fileName, log_b.log_date creationDate, su.name creationUserName,\n" +
                        "   case when drp.correction_date is not null then" +
                        "       tp.year || ': ' || rp.name || ', корр. (' || to_char(drp.correction_date, 'DD.MM.YYYY') || ')'" +
                        "       else tp.year || ': ' || rp.name end as reportPeriod,\n" +
                        "   dd.kpp, dd.oktmo, dd.tax_organ_code taxOrganCode, doc_state.name docState, dd.note\n" +
                        "from DECLARATION_DATA dd\n" +
                        "left join REF_BOOK_ASNU asnu on asnu.id = dd.asnu_id\n" +
                        "left join REF_BOOK_KNF_TYPE knf_type on knf_type.id = dd.knf_type_id\n" +
                        "inner join LOG_BUSINESS log_b on log_b.DECLARATION_DATA_ID = dd.id and log_b.event_id = 1\n" +
                        "inner join SEC_USER su on su.login = log_b.user_login\n" +
                        "inner join DECLARATION_TEMPLATE dt on dt.id = dd.declaration_template_id\n" +
                        "inner join DECLARATION_TYPE dtype on dtype.id = dt.declaration_type_id\n" +
                        "inner join DECLARATION_KIND dkind on dkind.id = dt.form_kind\n" +
                        "inner join DEPARTMENT_REPORT_PERIOD drp on drp.id = dd.department_report_period_id\n" +
                        "inner join REPORT_PERIOD rp on rp.id = drp.report_period_id\n" +
                        "inner join TAX_PERIOD tp on tp.id = rp.tax_period_id\n" +
                        "inner join STATE state on state.id = dd.state\n" +
                        "inner join DEPARTMENT dep on dep.id = drp.department_id\n" +
                        "inner join DEPARTMENT_FULLPATH dep_fullpath on dep_fullpath.id = dep.id\n" +
                        "left join REF_BOOK_DOC_STATE doc_state on doc_state.id = dd.doc_state_id\n" +
                        "where (:declarationDataId is null or dd.id like '%' || :declarationDataId || '%')\n" +
                        "   and (:state is null or dd.state = :state)\n" +
                        "   and (:fileName is null or upper(dd.file_name) like '%' || upper(:fileName) || '%')\n" +
                        "   and (:kpp is null or upper(dd.kpp) like '%' || upper(:kpp) || '%')\n" +
                        "   and (:oktmo is null or upper(dd.oktmo) like '%' || upper(:oktmo) || '%')\n" +
                        "   and (:note is null or upper(dd.note) like '%' || upper(:note) || '%')\n" +
                        "   and (:taxOrganCode is null or upper(dd.tax_organ_code) like '%' || upper(:taxOrganCode) || '%')\n"
        );
        params.addValue("declarationDataId", filter.getDeclarationDataId());
        params.addValue("state", filter.getFormState() != null ? filter.getFormState().getId() : null);
        params.addValue("fileName", filter.getFileName());
        params.addValue("kpp", filter.getTaxOrganKpp());
        params.addValue("oktmo", filter.getOktmo());
        params.addValue("note", filter.getNote());
        params.addValue("taxOrganCode", filter.getTaxOrganCode());

        if (filter.getCorrectionTag() != null) {
            if (filter.getCorrectionTag()) {
                sql.append(" and drp.correction_date is not null ");
            } else {
                sql.append(" and drp.correction_date is null ");
            }
        }

        if (!isEmpty(filter.getAsnuIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("asnu.id", filter.getAsnuIds()));
        }

        if (!isEmpty(filter.getKnfTypeIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("knf_type.id", filter.getKnfTypeIds()));
        }

        if (!isEmpty(filter.getDepartmentIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatementViaTmpTable("dep.id", filter.getDepartmentIds()));
        }

        if (!isEmpty(filter.getDeclarationTypeDepartmentMap())) {
            int index = 0;
            sql.append(" and (");
            for (Integer typeId : filter.getDeclarationTypeDepartmentMap().keySet()) {
                sql.append("dtype.id = ").append(typeId).append(" and ").append(SqlUtils.transformToSqlInStatement("dep.id", filter.getDeclarationTypeDepartmentMap().get(typeId)))
                        //если type не последний элемент в множестве добавлем оператор or, иначе закрываем скобку
                        .append(index++ != filter.getDeclarationTypeDepartmentMap().keySet().size() - 1 ? " or " : ")");
            }
        }

        if (!isEmpty(filter.getFormKindIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("dkind.id", filter.getFormKindIds()));
        }

        if (!isEmpty(filter.getDeclarationTypeIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("dtype.id", filter.getDeclarationTypeIds()));
        }

        if (!isEmpty(filter.getReportPeriodIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("rp.id", filter.getReportPeriodIds()));
        }

        if (!isEmpty(filter.getDocStateIds())) {
            sql.append(" and ").append(SqlUtils.transformToSqlInStatement("dd.doc_state_id", filter.getDocStateIds()));
        }

        String orderedSql = sql.toString() + " order by " + pagingParams.getProperty() + " " + pagingParams.getDirection();

        String numberedSql = "select rownum rn, ordered.* from (" + orderedSql + ") ordered";

        String pagedSql = "select * from (" + numberedSql + ") where rn between :start and :end";
        params.addValue("start", pagingParams.getStartIndex() + 1);
        params.addValue("end", pagingParams.getStartIndex() + pagingParams.getCount());

        List<DeclarationDataJournalItem> items = getNamedParameterJdbcTemplate().query(
                pagedSql, params,
                new RowMapper<DeclarationDataJournalItem>() {
                    @Override
                    public DeclarationDataJournalItem mapRow(ResultSet rs, int rowNum) throws SQLException {
                        DeclarationDataJournalItem item = new DeclarationDataJournalItem();
                        item.setDeclarationDataId(rs.getLong("declarationDataId"));
                        item.setDeclarationKind(rs.getString("declarationKind"));
                        item.setDeclarationType(rs.getString("declarationType"));
                        item.setDepartment(rs.getString("department"));
                        item.setAsnuName(rs.getString("asnuName"));
                        item.setKnfTypeName(rs.getString("knfTypeName"));
                        item.setState(rs.getString("state"));
                        item.setFileName(rs.getString("fileName"));
                        item.setCreationDate(new Date(rs.getTimestamp("creationDate").getTime()));
                        item.setCreationUserName(rs.getString("creationUserName"));
                        item.setReportPeriod(rs.getString("reportPeriod"));
                        item.setKpp(rs.getString("kpp"));
                        item.setOktmo(rs.getString("oktmo"));
                        item.setTaxOrganCode(rs.getString("taxOrganCode"));
                        item.setDocState(rs.getString("docState"));
                        item.setNote(rs.getString("note"));
                        return item;
                    }
                }
        );
        long count = getNamedParameterJdbcTemplate().queryForObject("select count(*) from (" + sql.toString() + ")", params, Long.class);

        return new PagingResult<>(items, (int) count);
    }

    @Override
    public List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        HashMap<String, Object> values = new HashMap<>();
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, values, declarationDataFilter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat");
        return getNamedParameterJdbcTemplate().query(
                sql.toString(),
                values,
                new RowMapper<Long>() {
                    @Override
                    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return SqlUtils.getLong(rs, "declaration_data_id");
                    }
                }
        );
    }

    @Override
    public long create(DeclarationData declarationData) {
        Assert.isTrue(declarationData.getId() == null, "Произведена попытка перезаписать уже сохранённую налоговую форму!");
        Assert.isTrue(!existDeclarationData(declarationData), "Налоговая форма с заданными параметрами уже существует!");

        long id = generateId("seq_declaration_data", Long.class);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("declaration_template_id", declarationData.getDeclarationTemplateId());
        params.addValue("department_report_period_id", declarationData.getDepartmentReportPeriodId());
        params.addValue("state", declarationData.getState().getId());
        params.addValue("tax_organ_code", declarationData.getTaxOrganCode());
        params.addValue("kpp", declarationData.getKpp());
        params.addValue("oktmo", declarationData.getOktmo());
        params.addValue("asnu_id", declarationData.getAsnuId());
        params.addValue("knf_type_id", declarationData.getKnfType() != null ? declarationData.getKnfType().getId() : null);
        params.addValue("note", declarationData.getNote());
        params.addValue("file_name", declarationData.getFileName());
        params.addValue("doc_state_id", declarationData.getDocState());
        params.addValue("manually_created", declarationData.isManuallyCreated());
        params.addValue("last_data_modified", declarationData.isManuallyCreated() ? null : new Date());
        params.addValue("adjust_negative_values", declarationData.isAdjustNegativeValues());

        getNamedParameterJdbcTemplate().update("" +
                        "insert into declaration_data (id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, " +
                        "   oktmo, asnu_id, knf_type_id, note, file_name, doc_state_id, manually_created, last_data_modified, adjust_negative_values) " +
                        "values (:id, :declaration_template_id, :department_report_period_id, :state, :tax_organ_code, :kpp, " +
                        "   :oktmo, :asnu_id, :knf_type_id, :note, :file_name, :doc_state_id, :manually_created, :last_data_modified, :adjust_negative_values)",
                params);

        if (declarationData.getKnfType() != null && declarationData.getKnfType().equals(RefBookKnfType.BY_KPP)) {
            createDeclarationDataKppList(id, declarationData.getIncludedKpps());
        }
        declarationData.setId(id);
        return id;
    }

    @Override
    public void setStatus(long declarationDataId, State state) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("state", state.getId());
        values.put("declarationDataId", declarationDataId);
        int count = getNamedParameterJdbcTemplate().update(
                "update declaration_data set state = :state where id = :declarationDataId",
                values
        );
        if (count == 0) {
            throw new DaoException("Не удалось изменить статус налоговой формы с id = %d, так как она не существует.", declarationDataId);
        }
    }

    @Override
    public void setDocStateId(long declarationDataId, Long docStateId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("docStateId", docStateId);
        values.put("declarationDataId", declarationDataId);
        int count = getNamedParameterJdbcTemplate().update(
                "update declaration_data set doc_state_id = :docStateId where id = :declarationDataId",
                values
        );
        if (count == 0) {
            throw new DaoException("Не удалось изменить статус налоговой формы с id = %d, так как она не существует.", declarationDataId);
        }
    }

    @Override
    public void setFileName(long declarationDataId, String fileName) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("fileName", fileName);
        values.put("declarationDataId", declarationDataId);
        int count = getNamedParameterJdbcTemplate().update(
                "update declaration_data set file_name = :fileName where id = :declarationDataId",
                values
        );
        if (count == 0) {
            throw new DaoException("Не удалось изменить имя налоговой формы с id = %d, так как она не существует.", declarationDataId);
        }
    }

    @Override
    public int getCount(DeclarationDataFilter filter) {
        StringBuilder sql = new StringBuilder("select count(*)");
        HashMap<String, Object> values = new HashMap<>();
        appendFromAndWhereClause(sql, values, filter);
        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), values, Integer.class);
    }

    private void appendFromAndWhereClause(StringBuilder sql, Map<String, Object> values, DeclarationDataFilter filter) {
        sql.append(" FROM declaration_data dec, department_report_period drp, declaration_type dectype, department dp, report_period rp, tax_period tp, declaration_template dectemplate")
                .append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
                .append(" AND drp.id = dec.department_report_period_id AND dp.id = drp.department_id AND rp.id = drp.report_period_id AND tp.id=rp.tax_period_id and dec.declaration_template_id = dectemplate.id");

        if (filter.getUserDepartmentId() != null && filter.getControlNs() != null) {
            if (!filter.getControlNs()) {
                sql.append(" AND (drp.department_id IN (SELECT dep_ddtp.ID FROM department dep_ddtp CONNECT BY PRIOR dep_ddtp.ID = dep_ddtp.parent_id  START WITH dep_ddtp.ID = :userDepId)")
                        .append(" OR :userDepId IN (\n" +
                                "SELECT DISTINCT ddtp.performer_dep_id \n" +
                                "FROM department_decl_type_performer ddtp \n" +
                                "INNER JOIN department_declaration_type ddt ON ddt.ID = ddtp.department_decl_type_id \n" +
                                "WHERE ddt.declaration_type_id = dectemplate.declaration_type_id AND ddt.department_id IN (SELECT dep_ddtp.ID FROM department dep_ddtp CONNECT BY PRIOR dep_ddtp.parent_id = dep_ddtp.ID START WITH dep_ddtp.ID = drp.department_id)\n" +
                                "))");
            } else {
                sql.append(" AND (drp.department_id IN (SELECT dep_ddtp.ID FROM department dep_ddtp CONNECT BY PRIOR dep_ddtp.ID = dep_ddtp.parent_id  START WITH dep_ddtp.ID = :userDepId)")
                        .append(" OR EXISTS (\n" +
                                "   SELECT ddtp.performer_dep_id \n" +
                                "   FROM department_decl_type_performer ddtp \n" +
                                "   INNER JOIN department_declaration_type ddt ON ddt.ID = ddtp.department_decl_type_id \n" +
                                "   WHERE ddt.declaration_type_id = dectemplate.declaration_type_id \n" +
                                "   AND ddt.department_id IN ( \n" +
                                "       SELECT dep_ddtp.ID " +
                                "       FROM department dep_ddtp \n" +
                                "       CONNECT BY PRIOR dep_ddtp.ID = dep_ddtp.parent_id \n" +
                                "       START WITH dep_ddtp.ID = ( \n" +
                                "                      SELECT dep.ID \n" +
                                "                      FROM department dep \n" +
                                "                      WHERE dep.parent_id = 0 and dep.type = 2 \n" +
                                "                      CONNECT BY PRIOR dep.parent_id = dep.id \n" +
                                "                      START WITH dep.id = drp.department_id \n" +
                                "                ) \n" +
                                "   ) \n" +
                                "INTERSECT \n " +
                                "   SELECT dep_ddtp.ID FROM department dep_ddtp CONNECT BY PRIOR dep_ddtp.ID = dep_ddtp.parent_id  START WITH dep_ddtp.ID = :userDepId) \n" +
                                ") \n");
            }
            values.put("userDepId", filter.getUserDepartmentId());
        }

        if (filter.getReportPeriodIds() != null && !filter.getReportPeriodIds().isEmpty()) {
            sql
                    .append(" AND ")
                    .append(transformToSqlInStatement("rp.id", filter.getReportPeriodIds()));
        }

        if (filter.getDepartmentIds() != null && !filter.getDepartmentIds().isEmpty()) {
            sql
                    .append(" AND ")
                    .append(transformToSqlInStatement("drp.department_id", filter.getDepartmentIds()));
        }

        if (filter.getDeclarationTypeIds() != null && !filter.getDeclarationTypeIds().isEmpty()) {
            sql.append(" AND ")
                    .append(transformToSqlInStatement("dectype.id", filter.getDeclarationTypeIds()));
        }

        if (filter.getFormState() != null) {
            sql.append(" AND dec.state = ").append(filter.getFormState().getId());
        }

        if (filter.getCorrectionTag() != null) {
            if (filter.getCorrectionDate() != null) {
                sql.append(" and drp.correction_date = '").append(new SimpleDateFormat("dd.MM.yyyy").format(filter.getCorrectionDate())).append("\'");
            } else {
                sql.append(" and drp.correction_date is ").append(Boolean.TRUE.equals(filter.getCorrectionTag()) ? "not " : "").append("null");
            }
        }

        if (filter.getAsnuIds() != null && !filter.getAsnuIds().isEmpty()) {
            sql.append(" AND ")
                    .append(SqlUtils.transformToSqlInStatement("dec.asnu_id", filter.getAsnuIds()));
        }
        if (filter.getFormKindIds() != null && !filter.getFormKindIds().isEmpty()) {
            sql.append(" AND ")
                    .append(SqlUtils.transformToSqlInStatement("dectemplate.form_kind", filter.getFormKindIds()));
        }
        if (filter.getFileName() != null && !filter.getFileName().isEmpty()) {
            sql.append(" AND lower(dec.file_name) like lower(:fileName)");
            values.put("fileName", "%" + filter.getFileName() + "%");
        }

        if (!StringUtils.isBlank(filter.getTaxOrganCode())) {
            sql.append(" AND lower(dec.tax_organ_code) like lower(:tax_organ_code)");
            values.put("tax_organ_code", "%" + filter.getTaxOrganCode() + "%");
        }

        if (!StringUtils.isBlank(filter.getTaxOrganKpp())) {
            sql.append(" AND lower(dec.kpp) like lower(:kpp)");
            values.put("kpp", "%" + filter.getTaxOrganKpp() + "%");
        }

        if (!StringUtils.isBlank(filter.getOktmo())) {
            sql.append(" AND lower(dec.oktmo) like lower(:oktmo)");
            values.put("oktmo", "%" + filter.getOktmo() + "%");
        }
        if (!StringUtils.isBlank(filter.getNote())) {
            sql.append(" AND lower(dec.note) like lower(:note)");
            values.put("note", "%" + filter.getNote() + "%");
        }

        if (filter.getDocStateIds() != null && !filter.getDocStateIds().isEmpty()) {
            sql.append(" AND ")
                    .append(SqlUtils.transformToSqlInStatement("dec.doc_state_id", filter.getDocStateIds()));
        }

        if (!StringUtils.isBlank(filter.getDeclarationDataIdStr())) {
            sql.append(" AND TO_CHAR(dec.id) like lower(:declarationDataIdStr)");
            values.put("declarationDataIdStr", "%" + filter.getDeclarationDataIdStr() + "%");
        }
    }

    private void appendSelectClause(StringBuilder sql) {
        sql.append("SELECT dec.ID as declaration_data_id, dec.declaration_template_id, dec.state, dec.tax_organ_code, dec.kpp, dec.oktmo, ")
                .append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
                .append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
                .append(" rp.ID as report_period_id, rp.NAME as report_period_name, tp.year, drp.correction_date, drp.is_active,")
                .append(" dec.asnu_id as asnu_id, dec.file_name as file_name, dec.doc_state_id, dec.note,")
                .append(" dectemplate.form_kind as form_kind, dectemplate.form_type as form_type,")
                .append(" (select bd.creation_date from declaration_report dr left join blob_data bd on bd.id = dr.blob_data_id where dr.declaration_data_id = dec.id and dr.type = 1) as creation_date,")
                .append(" (select ds.name from REF_BOOK_DOC_STATE ds where ds.id = dec.doc_state_id) as doc_state,").append(" (select lb.log_date from log_business lb where lb.event_id = ").append(FormDataEvent.CREATE.getCode()).append(" and lb.declaration_data_id = dec.id and rownum = 1) as decl_data_creation_date,")
                .append(" (select su.name from log_business lb join sec_user su on su.login=lb.user_login where lb.event_id = ").append(FormDataEvent.CREATE.getCode()).append(" and lb.declaration_data_id = dec.id and rownum = 1) as import_decl_data_user_name");
    }

    private void appendOrderByClause(StringBuilder sql, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
        sql.append(" order by ");

        String column = null;
        switch (ordering) {
            case ID:
                // Сортировка по ID делается всегда, поэтому здесь оставляем null
                break;
            case DEPARTMENT_NAME:
                column = "dp.name";
                break;
            case REPORT_PERIOD_NAME:
                column = "rp.name";
                break;
            case DECLARATION_TYPE_NAME:
                column = "dectype.name";
                break;
            case DECLARATION_STATE:
                column = "dec.state";
                break;
            case REPORT_PERIOD_YEAR:
                column = "rp.calendar_start_date";
                break;
            case ASNU:
                column = "dec.asnu_id";
                break;
            case DECLARATION_KIND_NAME:
                column = "dectemplate.form_kind";
                break;
            case FILE_NAME:
                column = "dec.file_name";
                break;
            case NOTE:
                column = "dec.note";
                break;
            case CREATE_DATE:
                column = "creation_date";
                break;
            case KPP:
                column = "dec.kpp";
                break;
            case TAX_ORGAN:
                column = "dec.tax_organ_code";
                break;
            case OKTMO:
                column = "dec.oktmo";
                break;
            case DOC_STATE:
                column = "doc_state";
                break;
            case DECLARATION_DATA_CREATE_DATE:
                column = "decl_data_creation_date";
                break;
            case IMPORT_USER_NAME:
                column = "import_decl_data_user_name";
                break;
        }

        if (column != null) {
            sql.append(column);
            if (!ascSorting) {
                sql.append(" desc");
            }
            sql.append(", ");
        }

        sql.append("dec.id");
        if (!ascSorting) {
            sql.append(" desc");
        }
    }

    @Override
    public List<Long> findDeclarationDataByFormTemplate(int templateId, Date startDate) {
        try {
            return getJdbcTemplate().queryForList(
                    "SELECT distinct dd.id FROM declaration_data dd, department_report_period drp " +
                            "WHERE drp.id = dd.department_report_period_id " +
                            "and dd.declaration_template_id = ? AND drp.report_period_id IN " +
                            "(SELECT id FROM report_period WHERE calendar_start_date >= ?)",
                    new Object[]{templateId, startDate},
                    new int[]{Types.NUMERIC, Types.DATE},
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        } catch (DataAccessException e) {
            LOG.error(String.format("Ошибка поиска налоговых форм для заданного шаблона %d", templateId), e);
            throw new DaoException("Ошибка поиска налоговых форм для заданного шаблона %d", templateId);
        }
    }

    @Override
    public List<Long> getDeclarationIds(int declarationTypeId, int departmentId) {
        try {
            return getJdbcTemplate().queryForList(
                    "select dec.id from declaration_data dec, department_report_period drp " +
                            "where drp.id = dec.department_report_period_id " +
                            "and exists (select 1 from declaration_template dt " +
                            "where dec.declaration_template_id=dt.id and dt.declaration_type_id = ?) " +
                            "and drp.department_id = ?",
                    new Object[]{
                            declarationTypeId,
                            departmentId
                    },
                    new int[]{
                            Types.NUMERIC,
                            Types.NUMERIC
                    },
                    Long.class
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        } catch (DataAccessException e) {
            String errorMsg = String.format("Ошибка при поиске налоговых форм по заданному сочетанию параметров: declarationTypeId = %d, departmentId = %d", declarationTypeId, departmentId);
            LOG.error(errorMsg, e);
            throw new DaoException(errorMsg);
        }
    }

    @Override
    public DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from " +
                            "(select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                            "dd.adjust_negative_values, drp.report_period_id, drp.department_id, rownum, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                            "from declaration_data dd " +
                            "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                            "inner join declaration_template dt on dt.id = dd.declaration_template_id\n" +
                            "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                            "where dt.declaration_type_id = ? " +
                            "and drp.department_id = ? " +
                            "and drp.report_period_id = ? " +
                            "order by drp.correction_date desc nulls last) " +
                            "where rownum = 1",
                    new Object[]{declarationTypeId, departmentId, reportPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Integer> findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("decTemplateId", decTemplateId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            return getNamedParameterJdbcTemplate().queryForList(FIND_DD_BY_RANGE_IN_RP, params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>(0);
        }
    }

    public void updateNote(long declarationDataId, String note) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("declarationDataId", declarationDataId);
        values.put("note", note);
        getNamedParameterJdbcTemplate().update("UPDATE declaration_data SET note = :note WHERE id = :declarationDataId", values);
    }

    @Override
    public String getNote(long declarationDataId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("declarationDataId", declarationDataId);
        return getNamedParameterJdbcTemplate().queryForObject("SELECT note FROM declaration_data WHERE id = :declarationDataId", values, String.class);
    }

    @Override
    public void updateLastDataModified(long declarationDataId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("declarationDataId", declarationDataId);
        values.put("last_data_modified", new Date());
        getNamedParameterJdbcTemplate().update("UPDATE declaration_data SET last_data_modified = :last_data_modified WHERE id = :declarationDataId", values);
    }

    @Override
    public List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId) {
        try {
            String select = "SELECT dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, dd.department_report_period_id, " +
                    "dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, dd.adjust_negative_values, drp.report_period_id, " +
                    "drp.department_id, rownum, knf_type.id AS knf_type_id, knf_type.name AS knf_type_name \n" +
                    "FROM declaration_data dd \n" +
                    "INNER JOIN department_report_period drp ON dd.department_report_period_id = drp.id\n" +
                    "INNER JOIN declaration_template dt ON dt.id = dd.declaration_template_id\n" +
                    "LEFT JOIN ref_book_knf_type knf_type ON knf_type.id = dd.knf_type_id\n" +
                    "WHERE dt.declaration_type_id           = ? \n" +
                    "AND drp.department_id                = ? \n" +
                    "AND drp.report_period_id             = ? \n" +
                    "ORDER BY drp.correction_date DESC NULLS LAST";
            return getJdbcTemplate().query(select,
                    new Object[]{declarationTypeId, departmentId, reportPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<DeclarationData> fetchAllDeclarationData(int declarationTypeId, List<Integer> departmentIds, int reportPeriodId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTypeId", declarationTypeId);
        params.addValue("departmentIds", departmentIds);
        params.addValue("reportPeriodId", reportPeriodId);
        return getNamedParameterJdbcTemplate().query(
                "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state,\n" +
                        " dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id,\n" +
                        " dd.manually_created, dd.adjust_negative_values, drp.report_period_id, drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name\n" +
                        "from DECLARATION_DATA dd\n" +
                        "inner join DECLARATION_TEMPLATE dt on dd.declaration_template_id = dt.id\n" +
                        "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                        "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                        "where declaration_type_id = :declarationTypeId\n" +
                        " and department_id in (:departmentIds)\n" +
                        " and report_period_id = :reportPeriodId",
                params,
                new DeclarationDataRowMapper());
    }

    @Override
    public DeclarationData findDeclarationDataByKppOktmoOfNdflPersonIncomes(int declarationTypeId, int departmentReportPeriodId, int departmentId, int reportPeriodId, String kpp, String oktmo) {
        String sql = "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, dd.manually_created, dd.adjust_negative_values, drp.report_period_id, " +
                "drp.department_id, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                "from DECLARATION_DATA dd " +
                "inner join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                "where dd.DEPARTMENT_REPORT_PERIOD_ID = :departmentReportPeriodId " +
                "and dd.DECLARATION_TEMPLATE_ID in " +
                "(select dt.id from DECLARATION_TEMPLATE dt" +
                " where dt.DECLARATION_TYPE_ID = :declarationTypeId) " +
                "and dd.id in (select np.declaration_data_id from NDFL_PERSON np " +
                "inner join declaration_data dd on dd.ID = np.DECLARATION_DATA_ID and np.id in " +
                "(select npi.ndfl_person_id from NDFL_PERSON_INCOME npi " +
                "inner join NDFL_PERSON np on np.id = npi.NDFL_PERSON_ID where lower(npi.kpp) = :kpp and npi.oktmo is null or lower(npi.oktmo) = :oktmo))";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTypeId", declarationTypeId)
                .addValue("departmentReportPeriodId", departmentReportPeriodId)
                .addValue("kpp", kpp != null ? kpp.toLowerCase() : null)
                .addValue("oktmo", oktmo != null ? oktmo.toLowerCase() : null);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<DeclarationData> findDeclarationDataByFileNameAndFileType(String fileName, Long fileTypeId) {
        String sql =
                "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                        "dd.department_report_period_id, dd.asnu_id, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                        "dd.adjust_negative_values, drp.report_period_id, drp.department_id, dd.note, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                        "from DECLARATION_DATA dd " +
                        "left join department_report_period drp on (dd.department_report_period_id = drp.id) " +
                        "inner join declaration_data_file ddf on (dd.id = ddf.declaration_data_id) " +
                        "inner join blob_data bd on (ddf.blob_data_id = bd.id) " +
                        "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                        "where lower(bd.name) LIKE :fileName and (:fileTypeId is null or ddf.file_type_id = :fileTypeId)";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("fileName", "%" + fileName.toLowerCase() + "%");
        params.addValue("fileTypeId", fileTypeId);
        return getNamedParameterJdbcTemplate().query(sql, params, new DeclarationDataRowMapper());
    }

    @Override
    public boolean existDeclarationData(long declarationDataId) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("declarationDataId", declarationDataId);
        try {
            return getNamedParameterJdbcTemplate().queryForObject("SELECT id FROM declaration_data WHERE id = :declarationDataId", values, Long.class) > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public List<DeclarationData> findAllActive(int declarationTypeId, int reportPeriodId) {
        String sql = "select " +
                "dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                "dd.department_report_period_id, dd.asnu_id, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                "dd.adjust_negative_values, drp.report_period_id, drp.department_id, dd.note, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                "from declaration_data dd " +
                "join department_report_period drp on drp.ID = dd.department_report_period_id " +
                "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                "where dd.declaration_template_id = :declarationTypeId " +
                "and drp.report_period_id = :reportPeriodId " +
                "and drp.is_active = 1 ";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTypeId", declarationTypeId)
                .addValue("reportPeriodId", reportPeriodId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<DeclarationData> find(int declarationTemplate, int departmentReportPeriodId, String taxOrganCode, String kpp, String oktmo) {
        String query = "select " +
                "dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                "dd.department_report_period_id, dd.asnu_id, dd.file_name, dd.doc_state_id, dd.manually_created, " +
                "dd.adjust_negative_values, drp.report_period_id, drp.department_id, dd.note, knf_type.id as knf_type_id, knf_type.name as knf_type_name " +
                "from declaration_data dd " +
                "join declaration_template dt on dt.id = dd.declaration_template_id " +
                "join department_report_period drp on drp.ID = dd.department_report_period_id " +
                "left join ref_book_knf_type knf_type on knf_type.id = dd.knf_type_id\n" +
                "where lower(dd.kpp) = :kpp and lower(dd.oktmo) = :oktmo and dt.id = :declarationTemplate " +
                "and drp.id = :departmentReportPeriodId and lower(dd.tax_organ_code) = :taxOrganCode";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTemplate", declarationTemplate)
                .addValue("departmentReportPeriodId", departmentReportPeriodId)
                .addValue("taxOrganCode", taxOrganCode != null ? taxOrganCode.toLowerCase() : null)
                .addValue("kpp", kpp != null ? kpp.toLowerCase() : null)
                .addValue("oktmo", oktmo != null ? oktmo.toLowerCase() : null);
        try {
            return getNamedParameterJdbcTemplate().query(query, params, new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Pair<String, String>> findNotPresentedPairKppOktmo(Long declarationDataId) {
        String sql = "select distinct npi.kpp, npi.oktmo " +
                "from ndfl_person_income npi " +
                "join ndfl_person np on npi.ndfl_person_id = np.id " +
                "join declaration_data dd on np.declaration_data_id = dd.ID " +
                "join department_report_period drp on dd.department_report_period_id = drp.id " +
                "join report_period rp on drp.report_period_id = rp.ID " +
                "where dd.id = :declarationDataId " +
                "and (npi.kpp, npi.oktmo) " +
                "not in (" +
                "   select rnd.kpp, ro.code " +
                "   from (select t.*, lead(t.version) over(partition by t.record_id order by version) - interval '1' DAY version_end from ref_book_ndfl_detail t) rnd " +
                "   join ref_book_oktmo ro on ro.id = rnd.oktmo " +
                "   where rnd.version <= rp.end_date AND (rnd.version_end IS NULL OR rp.end_date <= rnd.version_end)" +
                ")";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationDataId", declarationDataId);
        try {
            return getNamedParameterJdbcTemplate().query(sql, params, new RowMapper<Pair<String, String>>() {
                @Override
                public Pair<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                    String kpp = resultSet.getString("kpp");
                    String oktmo = resultSet.getString("oktmo");
                    return new Pair<>(kpp, oktmo);
                }
            });
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean existDeclarationData(DeclarationData declarationData) {
        JdbcTemplate jt = getJdbcTemplate();
        int countOfExisted = 0;

        switch (declarationData.getDeclarationTemplateId()) {
            case DeclarationType.NDFL_CONSOLIDATE:
                countOfExisted = jt.queryForObject("SELECT COUNT(id) FROM declaration_data " +
                                "WHERE declaration_template_id = ? AND department_report_period_id = ? AND knf_type_id = ?",
                        Integer.class, declarationData.getDeclarationTemplateId(), declarationData.getDepartmentReportPeriodId(), declarationData.getKnfType().getId());
                break;
            case DeclarationType.NDFL_PRIMARY:
                if (declarationData.isManuallyCreated()) {
                    countOfExisted = jt.queryForObject("SELECT COUNT(id) FROM declaration_data WHERE declaration_template_id = ?" +
                                    " AND department_report_period_id = ? AND manually_created = ? AND asnu_id = ?",
                            Integer.class, declarationData.getDeclarationTemplateId(), declarationData.getDepartmentReportPeriodId(), 1, declarationData.getAsnuId());
                }
                break;
            default:
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("declarationTemplateId", declarationData.getDeclarationTemplateId());
                params.addValue("taxOrganCode", declarationData.getTaxOrganCode() != null ? declarationData.getTaxOrganCode().toLowerCase() : null);
                params.addValue("departmentReportPeriodId", declarationData.getDepartmentReportPeriodId());
                params.addValue("kpp", declarationData.getKpp() != null ? declarationData.getKpp().toLowerCase() : null);
                params.addValue("oktmo", declarationData.getOktmo() != null ? declarationData.getOktmo().toLowerCase() : null);
                params.addValue("asnuId", declarationData.getAsnuId());
                params.addValue("taxOrganCode", declarationData.getTaxOrganCode() != null ? declarationData.getTaxOrganCode().toLowerCase() : null);
                params.addValue("note", declarationData.getNote() != null ? declarationData.getNote().toLowerCase() : null);
                params.addValue("fileName", declarationData.getFileName() != null ? declarationData.getFileName().toLowerCase() : null);
                params.addValue("docState", declarationData.getDocState());
                countOfExisted = getNamedParameterJdbcTemplate().queryForObject("SELECT COUNT(id) FROM declaration_data WHERE declaration_template_id = :declarationTemplateId" +
                                " AND department_report_period_id = :departmentReportPeriodId and (:taxOrganCode is null or lower(tax_organ_code) = :taxOrganCode) AND (:kpp is null or lower(kpp) = :kpp)" +
                                " AND (:oktmo is null or lower(oktmo) = :oktmo) AND (:asnuId is null or asnu_id = :asnuId) AND (:note is null or lower(note) = :note) " +
                                "AND (:fileName is null or lower(file_name) = :fileName) AND (:docState is null or doc_state_id = :docState)",
                        params, Integer.class);
        }

        return countOfExisted > 0;
    }

    private static final class DeclarationDataRowMapper implements RowMapper<DeclarationData> {
        @Override
        public DeclarationData mapRow(ResultSet rs, int index) throws SQLException {
            DeclarationData d = new DeclarationData();
            d.setId(SqlUtils.getLong(rs, "id"));
            d.setDeclarationTemplateId(SqlUtils.getInteger(rs, "declaration_template_id"));
            Integer knfTypeId = SqlUtils.getInteger(rs, "knf_type_id");
            if (knfTypeId != null) {
                d.setKnfType(new RefBookKnfType(knfTypeId, rs.getString("knf_type_name")));
            }
            d.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            d.setTaxOrganCode(rs.getString("tax_organ_code"));
            d.setKpp(rs.getString("kpp"));
            d.setOktmo(rs.getString("oktmo"));
            d.setReportPeriodId(SqlUtils.getInteger(rs, "report_period_id"));
            d.setDepartmentReportPeriodId(SqlUtils.getInteger(rs, "department_report_period_id"));
            d.setAsnuId(SqlUtils.getLong(rs, "asnu_id"));
            d.setNote(rs.getString("note"));
            d.setFileName(rs.getString("file_name"));
            d.setState(State.fromId(SqlUtils.getInteger(rs, "state")));
            d.setDocState(SqlUtils.getLong(rs, "doc_state_id"));
            d.setManuallyCreated(SqlUtils.getInteger(rs, "manually_created") == 1);
            d.setAdjustNegativeValues(SqlUtils.getInteger(rs, "adjust_negative_values") == 1);
            if (SqlUtils.isExistColumn(rs, "last_data_modified") && rs.getTimestamp("last_data_modified") != null) {
                d.setLastDataModifiedDate(new Date(rs.getTimestamp("last_data_modified").getTime()));
            }
            return d;
        }
    }

    @Override
    public List<Long> findApplication2DeclarationDataId(int reportYear) {
        String sql = "select max (dd.id) as dd_id from declaration_data dd\n" +
                "left join department_report_period drp on dd.department_report_period_id = drp.id\n" +
                "left join department dep on drp.department_id = dep.id\n" +
                "left join report_period rp on drp.report_period_id = rp.id\n" +
                "left join report_period_type rpt on rp.dict_tax_period_id = rpt.id\n" +
                "left join tax_period tp on rp.tax_period_id = tp.id\n" +
                "where dd.declaration_template_id = 101\n" +
                "and dep.id in (select id from department where dep.type = 2)\n" +
                "and dd.state = 3\n" +
                "and rpt.code = '34'\n" +
                "and tp.year = :reportYear\n" +
                "group by dep.id";
        MapSqlParameterSource params = new MapSqlParameterSource("reportYear", reportYear);
        try {
            return getNamedParameterJdbcTemplate().queryForList(sql, params, Long.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<>();
        }
    }
}
