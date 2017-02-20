package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils.transformToSqlInStatement;

/**
 * Реализация Dao для работы с декларациями
 *
 * @author dsultanbekov
 */
@Repository
@Transactional
public class DeclarationDataDaoImpl extends AbstractDao implements DeclarationDataDao {

    private static final Log LOG = LogFactory.getLog(DeclarationDataDaoImpl.class);
    private static final String DECLARATION_NOT_FOUND_MESSAGE = "Налоговая форма с id = %d не найдена в БД";
    private final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    private static final class DeclarationDataRowMapper implements RowMapper<DeclarationData> {
        @Override
        public DeclarationData mapRow(ResultSet rs, int index) throws SQLException {
            DeclarationData d = new DeclarationData();
            d.setId(SqlUtils.getLong(rs, "id"));
            d.setDeclarationTemplateId(SqlUtils.getInteger(rs, "declaration_template_id"));
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
            return d;
        }
    }

    @Override
    public DeclarationData get(long declarationDataId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id," +
                            "drp.report_period_id, drp.department_id " +
                            "from declaration_data dd, department_report_period drp " +
                            "where drp.id = dd.department_report_period_id and dd.id = ?",
                    new Object[]{declarationDataId},
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            throw new DaoException(DECLARATION_NOT_FOUND_MESSAGE, declarationDataId);
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
            return getJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, " +
                            "drp.report_period_id, drp.department_id " +
                            "from declaration_data dd, department_report_period drp " +
                            "where drp.id = dd.department_report_period_id and drp.id = ?" +
                            "and exists (select 1 from declaration_template dt where dd.declaration_template_id=dt.id " +
                            "and dt.declaration_type_id = ?) and (? is null or dd.kpp = ?) and (? is null or dd.oktmo = ?) " +
                            "and (? is null or dd.tax_organ_code = ?) " +
                            "and (? is null or asnu_id = ?) and (? is null or file_name = ?)",
                    new Object[]{
                            departmentReportPeriodId,
                            declarationTypeId,
                            kpp, kpp,
                            oktmo, oktmo,
                            taxOrganCode, taxOrganCode,
                            asnuId, asnuId,
                            fileName, fileName
                    },
                    new DeclarationDataRowMapper()
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
        return getJdbcTemplate().query(
                "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                        "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, " +
                        "drp.report_period_id, drp.department_id " +
                        "from declaration_data dd, department_report_period drp " +
                        "where drp.id = dd.department_report_period_id and file_name = ?",
                new Object[]{
                        fileName
                },
                new DeclarationDataRowMapper()
        );
    }

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId) {
        try {
            return getJdbcTemplate().query(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, " +
                            "drp.report_period_id, drp.department_id " +
                            "from declaration_data dd, department_report_period drp " +
                            "where drp.id = dd.department_report_period_id and drp.id = ?" +
                            "and exists (select 1 from declaration_template dt where dd.declaration_template_id=dt.id " +
                            "and dt.declaration_type_id = ?)",
                    new Object[]{
                            departmentReportPeriodId,
                            declarationTypeId
                    },
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<DeclarationData>();
        }
    }

    @Override
    public Long getRowNumByFilter(DeclarationDataFilter filter, DeclarationDataSearchOrdering ordering, boolean ascSorting, long declarationDataId) {
        StringBuilder sql = new StringBuilder("select rn from (select dat.*, rownum as rn from (");
        HashMap<String, Object> values = new HashMap<String, Object>();
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
        HashMap<String, Object> values = new HashMap<String, Object>();
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
        return new PagingResult<DeclarationDataSearchResultItem>(records, getCount(declarationFilter));
    }

    @Override
    public List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        HashMap<String, Object> values = new HashMap<String, Object>();
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
    public long saveNew(DeclarationData declarationData) {
        JdbcTemplate jt = getJdbcTemplate();

        Long id = declarationData.getId();
        if (id != null) {
            throw new DaoException("Произведена попытка перезаписать уже сохранённую налоговую форму!");
        }

        int countOfExisted = jt.queryForObject("SELECT COUNT(id) FROM declaration_data WHERE declaration_template_id = ?" +
                        " AND department_report_period_id = ? and (? is null or tax_organ_code = ?) AND (? is null or kpp = ?)" +
                        " AND (? is null or oktmo = ?) AND (? is null or asnu_id = ?) AND (? is null or note = ?) AND (? is null or file_name = ?) AND (? is null or doc_state_id = ?)",
                new Object[]{declarationData.getDeclarationTemplateId(), declarationData.getDepartmentReportPeriodId(),
                        declarationData.getTaxOrganCode(), declarationData.getTaxOrganCode(),
                        declarationData.getKpp(), declarationData.getKpp(), declarationData.getOktmo(), declarationData.getOktmo(),
                        declarationData.getAsnuId(), declarationData.getAsnuId(),
                        declarationData.getNote(), declarationData.getNote(),
                        declarationData.getFileName(), declarationData.getFileName(),
                        declarationData.getDocState(), declarationData.getDocState()},
                new int[]{Types.INTEGER, Types.INTEGER,
                        Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                        Types.INTEGER, Types.INTEGER,
                        Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR,
                        Types.INTEGER, Types.INTEGER},
                Integer.class);

        if (countOfExisted != 0) {
            throw new DaoException("Налоговая форма с заданными параметрами уже существует!");
        }

        id = generateId("seq_declaration_data", Long.class);
        jt.update(
                "insert into declaration_data (id, declaration_template_id, department_report_period_id, state, tax_organ_code, kpp, oktmo, asnu_id, note, file_name, doc_state_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                declarationData.getDeclarationTemplateId(),
                declarationData.getDepartmentReportPeriodId(),
                declarationData.getState().getId(),
                declarationData.getTaxOrganCode(),
                declarationData.getKpp(),
                declarationData.getOktmo(),
                declarationData.getAsnuId(),
                declarationData.getNote(),
                declarationData.getFileName(),
                declarationData.getDocState()
        );
        declarationData.setId(id);
        return id.longValue();
    }

    @Override
    public void setStatus(long declarationDataId, State state) {
        HashMap<String, Object> values = new HashMap<String, Object>();
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
    public void setFileName(long declarationDataId, String fileName) {
        HashMap<String, Object> values = new HashMap<String, Object>();
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
        HashMap<String, Object> values = new HashMap<String, Object>();
        appendFromAndWhereClause(sql, values, filter);
        return getNamedParameterJdbcTemplate().queryForObject(sql.toString(), values, Integer.class);
    }

    private void appendFromAndWhereClause(StringBuilder sql, Map<String, Object> values, DeclarationDataFilter filter) {
        sql.append(" FROM declaration_data dec, department_report_period drp, declaration_type dectype, department dp, report_period rp, tax_period tp, declaration_template dectemplate")
                .append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
                .append(" AND drp.id = dec.department_report_period_id AND dp.id = drp.department_id AND rp.id = drp.report_period_id AND tp.id=rp.tax_period_id and dec.declaration_template_id = dectemplate.id");

        if (filter.getTaxType() != null) {
            sql.append(" AND dectype.tax_type = ").append("\'").append(filter.getTaxType().getCode()).append("\'");
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
                sql.append(" and drp.correction_date = '" + sdf.get().format(filter.getCorrectionDate()) + "\'");
            } else {
                sql.append(" and drp.correction_date is " +
                        (Boolean.TRUE.equals(filter.getCorrectionTag()) ? "not " : "") + "null");
            }
        }

        if (filter.getAsnuId() != null) {
            sql.append(" AND dec.asnu_id = ").append(filter.getAsnuId());
        }
        if (filter.getFormKindIds() != null && !filter.getFormKindIds().isEmpty()) {
            sql.append(" AND ")
                    .append(SqlUtils.transformToSqlInStatement("dectemplate.form_kind", filter.getFormKindIds()));
        }
        if (filter.getFileName() != null && !filter.getFileName().isEmpty()) {
            sql.append(" AND lower(dec.file_name) like lower(:fileName)");
            values.put("fileName", "%" + filter.getFileName() + "%");
        }

        if (filter.getTaxType() == TaxType.NDFL || filter.getTaxType() == TaxType.PFR) {
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

            if (filter.getDocStateId() != null) {
                sql.append(" AND dec.doc_state_id = ").append(filter.getDocStateId());
            }
        }
    }

    private void appendSelectClause(StringBuilder sql) {
        sql.append("SELECT dec.ID as declaration_data_id, dec.declaration_template_id, dec.state, dec.tax_organ_code, dec.kpp, dec.oktmo, ")
                .append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
                .append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
                .append(" rp.ID as report_period_id, rp.NAME as report_period_name, dectype.TAX_TYPE, tp.year, drp.correction_date,")
                .append(" dec.asnu_id as asnu_id, dec.file_name as file_name, dec.doc_state_id, dec.note,")
                .append(" dectemplate.form_kind as form_kind, dectemplate.form_type as form_type,")
                .append(" (select bd.creation_date from declaration_report dr left join blob_data bd on bd.id = dr.blob_data_id where dr.declaration_data_id = dec.id and dr.type = 1) as creation_date,")
                .append(" (select ds.name from REF_BOOK_DOC_STATE ds where ds.id = dec.doc_state_id) as doc_state");
    }

    public void appendOrderByClause(StringBuilder sql, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
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
            return new ArrayList<Long>();
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
            return new ArrayList<Long>();
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
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, " +
                            "drp.report_period_id, drp.department_id, rownum " +
                            "from declaration_data dd, department_report_period drp, declaration_template dt " +
                            "where dd.department_report_period_id = drp.id " +
                            "and dt.id = dd.declaration_template_id " +
                            "and dt.declaration_type_id = ? " +
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
    public List<DeclarationData> getIfrs(int reportPeriodId) {
        try {
            return getJdbcTemplate().query(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                            "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, " +
                            "drp.report_period_id, drp.department_id " +
                            "from declaration_data dd, department_report_period drp, declaration_template dt, declaration_type t " +
                            "where drp.id = dd.department_report_period_id and drp.report_period_id = ? and " +
                            "dt.id = dd.declaration_template_id and t.id = dt.declaration_type_id and t.is_ifrs = 1 and drp.correction_date is null",
                    new Object[]{
                            reportPeriodId
                    },
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new DaoException(
                    "Для заданного сочетания параметров найдено несколько налоговых форм: reportPeriodId = %d",
                    reportPeriodId
            );
        }
    }

    private final static String FIND_DD_BY_RANGE_IN_RP =
            "select dd.id " +
                    " from DECLARATION_DATA dd \n" +
                    "  INNER JOIN DEPARTMENT_REPORT_PERIOD drp ON dd.DEPARTMENT_REPORT_PERIOD_ID = drp.ID\n" +
                    "  INNER JOIN REPORT_PERIOD rp ON drp.REPORT_PERIOD_ID = rp.ID\n" +
                    "  where dd.DECLARATION_TEMPLATE_ID = :decTemplateId and (rp.CALENDAR_START_DATE NOT BETWEEN :startDate AND :endDate\n" +
                    "    OR rp.END_DATE NOT BETWEEN :startDate AND :endDate)";

    @Override
    public List<Integer> findDDIdsByRangeInReportPeriod(int decTemplateId, Date startDate, Date endDate) {
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("decTemplateId", decTemplateId);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            return getNamedParameterJdbcTemplate().queryForList(FIND_DD_BY_RANGE_IN_RP, params, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            return new ArrayList<Integer>(0);
        }
    }

    public void updateNote(long declarationDataId, String note) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("declarationDataId", declarationDataId);
        values.put("note", note);
        getNamedParameterJdbcTemplate().update("UPDATE declaration_data SET note = :note WHERE id = :declarationDataId", values);
    }

    @Override
    public String getNote(long declarationDataId) {
        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put("declarationDataId", declarationDataId);
        return getNamedParameterJdbcTemplate().queryForObject("SELECT note FROM declaration_data WHERE id = :declarationDataId", values, String.class);
    }

    @Override
    public List<DeclarationData> findAllDeclarationData(int declarationTypeId, int departmentId, int reportPeriodId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, drp.report_period_id, drp.department_id, rownum \n");
            sb.append("FROM declaration_data dd, department_report_period drp, declaration_template dt \n");
            sb.append("WHERE dd.department_report_period_id = drp.id \n");
            sb.append("AND dt.id                            = dd.declaration_template_id \n");
            sb.append("AND dt.declaration_type_id           = ? \n");
            sb.append("AND drp.department_id                = ? \n");
            sb.append("AND drp.report_period_id             = ? \n");
            sb.append("ORDER BY drp.correction_date DESC nulls last");
            return getJdbcTemplate().query(sb.toString(),
                    new Object[]{declarationTypeId, departmentId, reportPeriodId},
                    new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC},
                    new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public DeclarationData findDeclarationDataByKppOktmoOfNdflPersonIncomes(int declarationTypeId, int departmentReportPeriodId, int departmentId, int reportPeriodId, String kpp, String oktmo) {
        String sql = "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.oktmo, dd.state, " +
                "dd.department_report_period_id, dd.asnu_id, dd.note, dd.file_name, dd.doc_state_id, drp.report_period_id, drp.department_id " +
                "from DEPARTMENT_REPORT_PERIOD drp, DECLARATION_DATA dd " +
                "where dd.DEPARTMENT_REPORT_PERIOD_ID = :departmentReportPeriodId and drp.IS_ACTIVE = 1" +
                "and drp.department_id = :departmentId and drp.REPORT_PERIOD_ID = :reportPeriodId " +
                "and dd.DECLARATION_TEMPLATE_ID in " +
                "(select dt.id from DECLARATION_TEMPLATE dt" +
                " where dt.DECLARATION_TYPE_ID = :declarationTypeId) " +
                "and dd.id in (select np.declaration_data_id from NDFL_PERSON np " +
                "inner join declaration_data dd on dd.ID = np.DECLARATION_DATA_ID and np.id in " +
                "(select npi.ndfl_person_id from NDFL_PERSON_INCOME npi " +
                "inner join NDFL_PERSON np on np.id = npi.NDFL_PERSON_ID where npi.kpp = :kpp and npi.oktmo is null or npi.oktmo = :oktmo))";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("declarationTypeId", declarationTypeId)
                .addValue("departmentReportPeriodId", departmentReportPeriodId)
                .addValue("departmentId", departmentId)
                .addValue("reportPeriodId", reportPeriodId)
                .addValue("kpp", kpp)
                .addValue("oktmo", oktmo);
        try {
            return getNamedParameterJdbcTemplate().queryForObject(sql, params, new DeclarationDataRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
