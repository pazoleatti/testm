package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.DeclarationDataDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.DeclarationDataSearchResultItemMapper;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
    private static final String DECLARATION_NOT_FOUND_MESSAGE = "Декларация с id = %d не найдена в БД";
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
            d.setReportPeriodId(SqlUtils.getInteger(rs, "report_period_id"));
            d.setDepartmentReportPeriodId(SqlUtils.getInteger(rs, "department_report_period_id"));
            d.setAccepted(rs.getBoolean("is_accepted"));
            return d;
        }
    }

    @Override
    public DeclarationData get(long declarationDataId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.is_accepted, " +
                            "dd.department_report_period_id, " +
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
            throw new DaoException("Не удалось удалить декларацию с id = %d, так как она не существует", id);
        }
    }

    @Override
    public DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String taxOrganCode) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.is_accepted, " +
                            "dd.department_report_period_id, " +
                            "drp.report_period_id, drp.department_id " +
                            "from declaration_data dd, department_report_period drp " +
                            "where drp.id = dd.department_report_period_id and drp.id = ?" +
                            "and exists (select 1 from declaration_template dt where dd.declaration_template_id=dt.id " +
                            "and dt.declaration_type_id = ?) and (? is null or dd.kpp = ?) and (? is null or dd.tax_organ_code = ?)",
                    new Object[]{
                            departmentReportPeriodId,
                            declarationTypeId,
                            kpp, kpp,
                            taxOrganCode, taxOrganCode
                    },
                    new DeclarationDataRowMapper()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new DaoException(
                    "Для заданного сочетания параметров найдено несколько деклараций: declarationTypeId = %d, departmentReportPeriodId = %d",
                    declarationTypeId,
                    departmentReportPeriodId
            );
        }
    }

    @Override
    public List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId) {
        try {
            return getJdbcTemplate().query(
                "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.is_accepted, " +
                        "dd.department_report_period_id, " +
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
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, filter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where declaration_data_id = ?");

        try {
            return getJdbcTemplate().queryForLong(
                    sql.toString(),
                    new Object[]{
                            declarationDataId
                    });
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public PagingResult<DeclarationDataSearchResultItem> findPage(DeclarationDataFilter declarationFilter, DeclarationDataSearchOrdering ordering,
                                                                  boolean ascSorting, PagingParams pageParams) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, declarationFilter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat where ordDat.rn between ? and ?")
                .append(" order by ordDat.rn");
        List<DeclarationDataSearchResultItem> records = getJdbcTemplate().query(
                sql.toString(),
                new Object[]{
                        pageParams.getStartIndex() + 1,    // В java нумерация с 0, в БД row_number() нумерует с 1
                        pageParams.getStartIndex() + pageParams.getCount()
                },
                new int[]{
                        Types.NUMERIC,
                        Types.NUMERIC
                },
                new DeclarationDataSearchResultItemMapper()
        );
        return new PagingResult<DeclarationDataSearchResultItem>(records, getCount(declarationFilter));
    }

    @Override
    public List<Long> findIdsByFilter(DeclarationDataFilter declarationDataFilter, DeclarationDataSearchOrdering ordering, boolean ascSorting) {
        StringBuilder sql = new StringBuilder("select ordDat.* from (select dat.*, rownum as rn from (");
        appendSelectClause(sql);
        appendFromAndWhereClause(sql, declarationDataFilter);
        appendOrderByClause(sql, ordering, ascSorting);
        sql.append(") dat) ordDat");
        return getJdbcTemplate().query(
                sql.toString(),
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
            throw new DaoException("Произведена попытка перезаписать уже сохранённую декларацию!");
        }

        int countOfExisted = jt.queryForInt("SELECT COUNT(*) FROM declaration_data WHERE declaration_template_id = ?" +
                " AND department_report_period_id = ? and (? is null or tax_organ_code = ?) and (? is null or kpp = ?)",
                new Object[]{declarationData.getDeclarationTemplateId(), declarationData.getDepartmentReportPeriodId(),
                        declarationData.getTaxOrganCode(), declarationData.getTaxOrganCode(), declarationData.getKpp(),
                        declarationData.getKpp()},
                new int[]{Types.INTEGER, Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR});

        if (countOfExisted != 0) {
            throw new DaoException("Декларация с заданными параметрами уже существует!");
        }

        id = generateId("seq_declaration_data", Long.class);
        jt.update(
                "insert into declaration_data (id, declaration_template_id, department_report_period_id, is_accepted, tax_organ_code, kpp) values (?, ?, ?, ?, ?, ?)",
                id,
                declarationData.getDeclarationTemplateId(),
                declarationData.getDepartmentReportPeriodId(),
                declarationData.isAccepted() ? 1 : 0,
                declarationData.getTaxOrganCode(),
                declarationData.getKpp()
        );
        declarationData.setId(id);
        return id.longValue();
    }

    @Override
    public void setAccepted(long declarationDataId, boolean accepted) {
        int count = getJdbcTemplate().update(
                "update declaration_data set is_accepted = ? where id = ?",
                accepted,
                declarationDataId
        );
        if (count == 0) {
            throw new DaoException("Не удалось изменить статус декларации с id = %d, так как она не существует.", declarationDataId);
        }
    }

    @Override
    public int getCount(DeclarationDataFilter filter) {
        StringBuilder sql = new StringBuilder("select count(*)");
        appendFromAndWhereClause(sql, filter);
        return getJdbcTemplate().queryForInt(sql.toString());
    }

    private void appendFromAndWhereClause(StringBuilder sql, DeclarationDataFilter filter) {
        sql.append(" FROM declaration_data dec, department_report_period drp, declaration_type dectype, department dp, report_period rp, tax_period tp")
                .append(" WHERE EXISTS (SELECT 1 FROM DECLARATION_TEMPLATE dectemp WHERE dectemp.id = dec.declaration_template_id AND dectemp.declaration_type_id = dectype.id)")
                .append(" and drp.id = dec.department_report_period_id AND dp.id = drp.department_id AND rp.id = drp.report_period_id AND tp.id=rp.tax_period_id");

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

        if (filter.getDeclarationTypeId() != null) {
            sql.append(" AND dectype.id = ").append(filter.getDeclarationTypeId());
        }

        if (filter.getFormState() != null) {
            if (filter.getFormState() == WorkflowState.CREATED) {
                sql.append(" AND dec.is_accepted = 0");
            } else if (filter.getFormState() == WorkflowState.ACCEPTED) {
                sql.append(" AND dec.is_accepted = 1");
            }
        }

        if (filter.getCorrectionTag() != null) {
            if (filter.getCorrectionDate() != null) {
                sql.append(" and drp.correction_date = '" + sdf.get().format(filter.getCorrectionDate()) + "\'");
            } else {
                sql.append(" and drp.correction_date is " +
                        (Boolean.TRUE.equals(filter.getCorrectionTag()) ? "not " : "") + "null");
            }
        }

        if (filter.getTaxType() == TaxType.PROPERTY || filter.getTaxType() == TaxType.TRANSPORT || filter.getTaxType() == TaxType.INCOME || filter.getTaxType() == TaxType.LAND || filter.getTaxType() == TaxType.NDFL || filter.getTaxType() == TaxType.PFR) {
            if (filter.getTaxOrganCode() != null && !filter.getTaxOrganCode().isEmpty()) {
                String[] codes = filter.getTaxOrganCode().split("; ");
                for (int i = 0; i < codes.length; i++) {
                    codes[i] = "'" + codes[i].trim() + "'";
                }
                if (codes.length != 0) {
                    sql.append(" AND ").append(SqlUtils.transformToSqlInStatement("dec.tax_organ_code", Arrays.asList(codes)));
                }
            }

            if (filter.getTaxOrganKpp() != null && !filter.getTaxOrganKpp().isEmpty()) {
                String[] codes = filter.getTaxOrganKpp().split("; ");
                for (int i = 0; i < codes.length; i++) {
                    codes[i] = "'" + codes[i].trim() + "'";
                }
                if (codes.length != 0) {
                    sql.append(" AND ").append(SqlUtils.transformToSqlInStatement("dec.kpp", Arrays.asList(codes)));
                }
            }
        }
    }

    private void appendSelectClause(StringBuilder sql) {
        sql.append("SELECT dec.ID as declaration_data_id, dec.declaration_template_id, dec.is_accepted, dec.tax_organ_code, dec.kpp,")
                .append(" dectype.ID as declaration_type_id, dectype.NAME as declaration_type_name,")
                .append(" dp.ID as department_id, dp.NAME as department_name, dp.TYPE as department_type,")
                .append(" rp.ID as report_period_id, rp.NAME as report_period_name, dectype.TAX_TYPE, tp.year, drp.correction_date");
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
            case DECLARATION_ACCEPTED:
                column = "dec.is_accepted";
                break;
            case REPORT_PERIOD_YEAR:
                column = "rp.calendar_start_date";
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
			LOG.error(String.format("Ошибка поиска деклараций для заданного шаблона %d", templateId), e);
            throw new DaoException("Ошибка поиска деклараций для заданного шаблона %d", templateId);
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
            String errorMsg = String.format("Ошибка при поиске деклараций по заданному сочетанию параметров: declarationTypeId = %d, departmentId = %d", declarationTypeId, departmentId);
			LOG.error(errorMsg, e);
            throw new DaoException(errorMsg);
        }
    }

    @Override
    public DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select * from " +
                            "(select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.is_accepted, " +
                            "dd.department_report_period_id, " +
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
                    "select dd.id, dd.declaration_template_id, dd.tax_organ_code, dd.kpp, dd.is_accepted, " +
                            "dd.department_report_period_id, " +
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
                    "Для заданного сочетания параметров найдено несколько деклараций: reportPeriodId = %d",
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
        } catch (EmptyResultDataAccessException e){
            return new ArrayList<Integer>(0);
        }
    }
}
