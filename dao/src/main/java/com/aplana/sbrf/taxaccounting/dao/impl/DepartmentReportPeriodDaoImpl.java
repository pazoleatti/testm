package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;


@Repository
public class DepartmentReportPeriodDaoImpl extends AbstractDao implements DepartmentReportPeriodDao {

    private static final Log LOG = LogFactory.getLog(DepartmentReportPeriodDaoImpl.class);

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    /**
     * Запрос обычной выборки отчетных периодов подразделения
     */
    private static final String QUERY_TEMPLATE_SIMPLE = "select id, department_id, report_period_id, is_active, correction_date from department_report_period";

    /**
     * Запрос выборки отчетных периодов подразделения с сортировкой по году и периоду сдаче корректировки(сначала некорректирующие)
     */
    private static final String QUERY_TEMPLATE_COMPOSITE_SORT = "select \n" +
            " drp.id, rpt.code, drp.department_id, drp.report_period_id, drp.is_active, drp.correction_date, rp.form_type_id \n" +
            " from \n" +
            " department_report_period drp \n" +
            " join report_period rp on drp.report_period_id = rp.id \n" +
            " join tax_period tp on rp.tax_period_id = tp.id \n" +
            " join report_period_type rpt on rpt.id = rp.dict_tax_period_id\n" +
            " %s \n" +
            " order by tp.year, drp.correction_date NULLS FIRST";

    /**
     * Запрос выборки идентификаторов отчетных периодов подразделения
     */
    private static final String QUERY_TEMPLATE_COMPOSITE_SORT_ID = "select drp.id from department_report_period drp " +
            "join report_period rp on drp.report_period_id = rp.id \n" +
            "join tax_period tp on rp.tax_period_id = tp.id " +
            " %s ";

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link DepartmentReportPeriod}
     */
    private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
        @Override
        public DepartmentReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
            reportPeriod.setId(SqlUtils.getInteger(rs, "id"));
            reportPeriod.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            reportPeriod.setReportPeriod(reportPeriodDao.fetchOne(SqlUtils.getInteger(rs, "report_period_id")));
            reportPeriod.setIsActive(!Objects.equals(SqlUtils.getInteger(rs, "is_active"), 0));
            reportPeriod.setCorrectionDate(rs.getDate("correction_date"));
            return reportPeriod;
        }
    };

    /**
     * Маппер для представления значений из {@link ResultSet} в виде объекта {@link DepartmentReportPeriodJournalItem}
     */
    private final RowMapper<DepartmentReportPeriodJournalItem> mapperJournalItem = new RowMapper<DepartmentReportPeriodJournalItem>() {
        @Override
        public DepartmentReportPeriodJournalItem mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriodJournalItem departmentReportPeriodJournalItem = new DepartmentReportPeriodJournalItem();
            departmentReportPeriodJournalItem.setId(SqlUtils.getInteger(rs, "id"));
            departmentReportPeriodJournalItem.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            ReportPeriod reportPeriod = reportPeriodDao.fetchOne(SqlUtils.getInteger(rs, "report_period_id"));
            departmentReportPeriodJournalItem.setReportPeriodId(reportPeriod.getId());
            departmentReportPeriodJournalItem.setDictTaxPeriodId(reportPeriod.getDictTaxPeriodId());
            departmentReportPeriodJournalItem.setYear(reportPeriod.getTaxPeriod().getYear());
            departmentReportPeriodJournalItem.setName(reportPeriod.getName());
            departmentReportPeriodJournalItem.setEndDate(reportPeriod.getEndDate());
            departmentReportPeriodJournalItem.setIsActive(!Objects.equals(SqlUtils.getInteger(rs, "is_active"), 0));
            departmentReportPeriodJournalItem.setCorrectionDate(rs.getDate("correction_date"));
            departmentReportPeriodJournalItem.setCode(rs.getString("code"));
            departmentReportPeriodJournalItem.setTaxFormTypeId(SqlUtils.getInteger(rs, "form_type_id"));
            return departmentReportPeriodJournalItem;
        }
    };

    /**
     * Формирует sql выражение where с параметрами из фильтра
     *
     * @param filter фильтр
     * @return строка с условиями выборки where
     */
    private String makeSqlWhereClause(DepartmentReportPeriodFilter filter) {
        if (filter == null) {
            return "";
        }

        List<String> causeList = new LinkedList<>();

        if (filter.isCorrection() != null) {
            causeList.add("drp.correction_date is " + (filter.isCorrection() ? " not " : "") + " null");
        }

        if (filter.isActive() != null) {
            causeList.add("drp.is_active " + (filter.isActive() ? "<>" : "=") + " 0");
        }

        if (filter.getCorrectionDate() != null) {
            causeList.add("drp.correction_date = to_date('" +
                    SIMPLE_DATE_FORMAT.get().format(filter.getCorrectionDate()) +
                    "', 'DD.MM.YYYY')");
        }

        if (filter.getDepartmentIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.department_id",
                    filter.getDepartmentIdList()));
        }

        if (filter.getReportPeriodIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.report_period_id",
                    filter.getReportPeriodIdList()));
        }

        if (filter.getTaxTypeList() != null) {
            causeList.add("tp.tax_type in " +
                    SqlUtils.transformTaxTypeToSqlInStatement(filter.getTaxTypeList()));
        }
        if (filter.getYearStart() != null || filter.getYearEnd() != null) {
            causeList.add("(:yearStart is null or tp.year >= :yearStart) and (:yearEnd is null or tp.year <= :yearEnd)");
        }

        if (filter.getDepartmentId() != null) {
            causeList.add("drp.department_id = " + filter.getDepartmentId());
        }

        if (filter.getReportPeriod() != null) {
            if (filter.getReportPeriod().getId() != null) {
                causeList.add("drp.report_period_id = " + filter.getReportPeriod().getId());
            } else if (filter.getReportPeriod().getReportPeriodTaxFormTypeId() != null) {
                causeList.add("rp.form_type_id = " + filter.getReportPeriod().getReportPeriodTaxFormTypeId());
            }
        }

        if (causeList.isEmpty()) {
            return "";
        }

        return " where " + StringUtils.join(causeList, " and ");
    }

    /**
     * Выполняет запрос в БД  с фильтрацией и формирует список объектов по указанному мапперу
     *
     * @param filter фильтр
     * @param mapper маппер
     * @param <T>    целевой класс для выборки {@link DepartmentReportPeriod} или {@link DepartmentReportPeriodJournalItem}
     * @return список объектов типа <T> или пустой список
     */
    private <T> List<T> executeFetchByFilter(final DepartmentReportPeriodFilter filter, RowMapper<T> mapper) {
        return getNamedParameterJdbcTemplate().query(String.format(QUERY_TEMPLATE_COMPOSITE_SORT, makeSqlWhereClause(filter)),
                new HashMap<String, Object>(2) {{
                    put("yearStart", filter.getYearStart());
                    put("yearEnd", filter.getYearEnd());
                }}, mapper);
    }

    @Override
    public List<DepartmentReportPeriod> fetchAllByFilter(final DepartmentReportPeriodFilter filter) {
        return executeFetchByFilter(filter, mapper);
    }

    @Override
    public List<Integer> fetchAllIdsByFilter(final DepartmentReportPeriodFilter filter) {
        return getNamedParameterJdbcTemplate().queryForList(
                String.format(QUERY_TEMPLATE_COMPOSITE_SORT_ID, makeSqlWhereClause(filter)),
                new HashMap<String, Object>(2) {{
                    put("yearStart", filter.getYearStart());
                    put("yearEnd", filter.getYearEnd());
                }}, Integer.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(final DepartmentReportPeriodFilter filter) {
        return executeFetchByFilter(filter, mapperJournalItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#departmentReportPeriod.id")
    public void create(DepartmentReportPeriod departmentReportPeriod) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("DEPARTMENT_ID", departmentReportPeriod.getDepartmentId());
        params.addValue("REPORT_PERIOD_ID", departmentReportPeriod.getReportPeriod().getId());
        params.addValue("IS_ACTIVE", departmentReportPeriod.isActive());
        params.addValue("CORRECTION_DATE", departmentReportPeriod.getCorrectionDate());

        getNamedParameterJdbcTemplate().update(
                "INSERT INTO DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, CORRECTION_DATE)" +
                        " VALUES (seq_department_report_period.nextval, :DEPARTMENT_ID, :REPORT_PERIOD_ID, :IS_ACTIVE, :CORRECTION_DATE)",
                params, keyHolder, new String[]{"ID"});
        departmentReportPeriod.setId(keyHolder.getKey().intValue());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void create(final DepartmentReportPeriod departmentReportPeriod, final List<Integer> departmentIds) {
        final List<Integer> ids = generateIds("seq_department_report_period", departmentIds.size(), Integer.class);
        getJdbcTemplate().batchUpdate("INSERT INTO DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, CORRECTION_DATE)" +
                " VALUES (?, ?, ?, ?, ?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, ids.get(i).intValue());
                ps.setInt(2, departmentIds.get(i));
                ps.setInt(3, departmentReportPeriod.getReportPeriod().getId());
                ps.setBoolean(4, departmentReportPeriod.isActive());
                ps.setDate(5, departmentReportPeriod.getCorrectionDate() != null ?
                        new java.sql.Date(departmentReportPeriod.getCorrectionDate().getTime()) : null);
            }

            @Override
            public int getBatchSize() {
                return departmentIds.size();
            }
        });
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void merge(final List<DepartmentReportPeriod> departmentReportPeriods, final Integer departmentId) {
        final List<Integer> ids = generateIds("seq_department_report_period", departmentReportPeriods.size(), Integer.class);
        getJdbcTemplate().batchUpdate("MERGE INTO DEPARTMENT_REPORT_PERIOD dest " +
                (isSupportOver() ?
                        "USING (SELECT ? id, ? department_id, ? report_period_id, ? is_active, ? correction_date FROM DUAL) src " :
                        "USING (SELECT cast(? as number) id, cast(? as number) department_id, cast(? as number) report_period_id, cast(? as number) is_active, ? correction_date FROM DUAL) src "
                ) +
                "on (dest.department_id = src.department_id and dest.report_period_id = src.report_period_id and decode(dest.correction_date, src.correction_date, 1, 0) = 1) " +
                "WHEN NOT MATCHED THEN " +
                " INSERT(id, department_id, report_period_id, is_active, correction_date)" +
                " VALUES(src.id, src.department_id, src.report_period_id, src.is_active, src.correction_date)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, ids.get(i));
                ps.setInt(2, departmentId);
                DepartmentReportPeriod departmentReportPeriod = departmentReportPeriods.get(i);
                ps.setInt(3, departmentReportPeriod.getReportPeriod().getId());
                ps.setBoolean(4, departmentReportPeriod.isActive());
                ps.setDate(5, departmentReportPeriod.getCorrectionDate() != null ?
                        new java.sql.Date(departmentReportPeriod.getCorrectionDate().getTime()) : null);
            }

            @Override
            public int getBatchSize() {
                return departmentReportPeriods.size();
            }
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#id")
    public void updateActive(int id, boolean active) {
        getJdbcTemplate().update(
                "UPDATE department_report_period SET is_active = ? WHERE id = ?",
                new Object[]{active ? 1 : 0, id},
                new int[]{Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void updateActive(final List<Integer> ids, final Integer reportPeriodId, final boolean active) {
        getJdbcTemplate().batchUpdate("UPDATE department_report_period SET is_active = ? WHERE report_period_id = ? AND id = ?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, active ? 1 : 0);
                ps.setInt(2, reportPeriodId);
                ps.setInt(3, ids.get(i));
            }

            @Override
            public int getBatchSize() {
                return ids.size();
            }
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void delete(final List<Integer> ids) {
        getJdbcTemplate().batchUpdate("DELETE FROM department_report_period WHERE id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, ids.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return ids.size();
                    }
                }
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsByReportPeriodIdAndDepartmentId(int departmentId, int reportPeriodId) {
        Integer count = getJdbcTemplate().queryForObject(
                "SELECT CASE WHEN exists(SELECT * FROM department_report_period WHERE department_id = ? AND report_period_id = ?) THEN 1 ELSE 0 END FROM dual",
                Integer.class, departmentId, reportPeriodId
        );
        return count != 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsByReportPeriodId(int reportPeriodId) {
        Integer count = getJdbcTemplate().queryForObject(
                "SELECT CASE WHEN exists(SELECT * FROM department_report_period WHERE report_period_id = ?) THEN 1 ELSE 0 END FROM dual",
                Integer.class, reportPeriodId);
        return count != 0;
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "FROM " +
                            "department_report_period drp, " +
                            "(SELECT max(correction_date) AS correction_date, department_id, report_period_id " +
                            "FROM department_report_period " +
                            "WHERE department_id = ? AND report_period_id = ? " +
                            "GROUP BY department_id, report_period_id) m " +
                            "WHERE drp.department_id = m.department_id " +
                            "AND drp.report_period_id = m.report_period_id " +
                            "AND (drp.correction_date = m.correction_date OR (m.correction_date IS NULL " +
                            "AND drp.correction_date IS NULL))",
                    new Object[]{departmentId, reportPeriodId}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchPrevLast(int departmentId, int reportPeriodId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("departmentId", departmentId).
                    addValue("reportPeriodId", reportPeriodId);
            return getNamedParameterJdbcTemplate().queryForObject("SELECT drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date FROM department_report_period drp " +
                            "WHERE drp.REPORT_PERIOD_ID = :reportPeriodId AND drp.DEPARTMENT_ID = :departmentId AND " +
                            "drp.CORRECTION_DATE IN (SELECT max(CORRECTION_DATE) FROM department_report_period WHERE " +
                            "REPORT_PERIOD_ID = :reportPeriodId AND DEPARTMENT_ID = :departmentId AND CORRECTION_DATE " +
                            "NOT IN (SELECT max(CORRECTION_DATE) FROM department_report_period WHERE " +
                            "REPORT_PERIOD_ID = :reportPeriodId AND DEPARTMENT_ID = :departmentId))",
                    params, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchFirst(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("SELECT drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "FROM " +
                            "department_report_period drp " +
                            "WHERE drp.department_id = ? " +
                            "AND drp.report_period_id = ? " +
                            "AND drp.correction_date IS NULL",
                    new Object[]{departmentId, reportPeriodId}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLaterCorrectionPeriodExists(DepartmentReportPeriod departmentReportPeriod) {
        return getJdbcTemplate().queryForObject(
                "SELECT count(*) FROM department_report_period drp\n " +
                        "join report_period rp on rp.id = drp.report_period_id\n " +
                        "WHERE\n " +
                        "drp.DEPARTMENT_ID = ?\n" +
                        "AND drp.report_period_id = ? AND drp.CORRECTION_DATE > ? AND rp.form_type_id = ?",
                Integer.class, departmentReportPeriod.getDepartmentId(),
                departmentReportPeriod.getReportPeriod().getId(), departmentReportPeriod.getCorrectionDate(),
                departmentReportPeriod.getReportPeriod().getReportPeriodTaxFormTypeId()
        ) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> fetchIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId) {
        String query = "SELECT drp.id FROM department_report_period drp \n" +
                "JOIN department_report_period drp2 ON drp2.id = :departmentReportPeriodId\n" +
                "WHERE drp.department_id IN (SELECT id FROM department WHERE type = :departmentType) \n" +
                "AND drp.report_period_id = drp2.report_period_id " +
                "AND (drp.correction_date IS NULL AND drp2.correction_date IS NULL OR drp.correction_date = drp2.correction_date)";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("departmentType", departmentTypeCode);
        params.addValue("departmentReportPeriodId", departmentReportPeriodId);
        try {
            return getNamedParameterJdbcTemplate().queryForList(query, params, Integer.class);
        } catch (EmptyResultDataAccessException ex) {
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#id")
    public DepartmentReportPeriod fetchOne(int id) {
        try {
            return getJdbcTemplate().queryForObject(QUERY_TEMPLATE_SIMPLE + " WHERE id = ?", new Object[]{id}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public SecuredEntity findSecuredEntityById(long id) {
        return fetchOne((int) id);
    }
}