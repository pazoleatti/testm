package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.CacheConstants;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriodJournalItem;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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

    private ReportPeriodDao reportPeriodDao;


    public DepartmentReportPeriodDaoImpl(ReportPeriodDao reportPeriodDao) {
        this.reportPeriodDao = reportPeriodDao;
    }

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    /**
     * Запрос обычной выборки отчетных периодов подразделения
     */
    private static final String QUERY_TEMPLATE_SIMPLE = "select id, department_id, report_period_id, is_active, " +
            "correction_date from department_report_period";

    /**
     * Запрос выборки отчетных периодов подразделения с сортировкой по году и периоду сдаче корректировки(сначала некорректирующие)
     */
    private static final String QUERY_TEMPLATE_COMPOSITE_SORT = "select drp.id, drp.department_id, drp.report_period_id, drp.is_active, drp.correction_date \n" +
            "          from \n" +
            "          department_report_period drp \n" +
            "          join report_period rp on drp.report_period_id = rp.id \n" +
            "          join tax_period tp on rp.tax_period_id = tp.id \n" +
            "            %s \n" +
            "            order by tp.year, drp.CORRECTION_DATE NULLS FIRST";

    /**
     * Запрос выборки идентификаторов отчетных периодов подразделения
     */
    private static final String QUERY_TEMPLATE_COMPOSITE_SORT_ID =
            "select drp.id from department_report_period drp " +
                    "join report_period rp on drp.report_period_id = rp.id \n" +
                    "join tax_period tp on rp.tax_period_id = tp.id " +
                    " %s ";


    /**
     * Маппер для {@link DepartmentReportPeriod} представления значений из {@link ResultSet} в объект отчетного периода подразделения
     */
    private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
        @Override
        public DepartmentReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
            reportPeriod.setId(SqlUtils.getInteger(rs, "id"));
            reportPeriod.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            reportPeriod.setReportPeriod(reportPeriodDao.get(SqlUtils.getInteger(rs, "report_period_id")));
            reportPeriod.setIsActive(!Objects.equals(SqlUtils.getInteger(rs, "is_active"), 0));
            reportPeriod.setCorrectionDate(rs.getDate("correction_date"));
            return reportPeriod;
        }
    };

    /**
     * Маппер для {@link DepartmentReportPeriodJournalItem} представления значений из {@link ResultSet}
     * в объект модели отчетного периода подразделения для отображения на клиенте
     */
    private final RowMapper<DepartmentReportPeriodJournalItem> mapperJournalItem = new RowMapper<DepartmentReportPeriodJournalItem>() {
        @Override
        public DepartmentReportPeriodJournalItem mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriodJournalItem departmentReportPeriodJournalItem = new DepartmentReportPeriodJournalItem();
            departmentReportPeriodJournalItem.setId(SqlUtils.getInteger(rs, "id"));
            departmentReportPeriodJournalItem.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            ReportPeriod reportPeriod = reportPeriodDao.get(SqlUtils.getInteger(rs, "report_period_id"));
            departmentReportPeriodJournalItem.setReportPeriodId(reportPeriod.getId());
            departmentReportPeriodJournalItem.setDictTaxPeriodId(reportPeriod.getDictTaxPeriodId());
            departmentReportPeriodJournalItem.setYear(reportPeriod.getTaxPeriod().getYear());
            departmentReportPeriodJournalItem.setName(reportPeriod.getName());
            departmentReportPeriodJournalItem.setIsActive(!Objects.equals(SqlUtils.getInteger(rs, "is_active"), 0));
            departmentReportPeriodJournalItem.setCorrectionDate(rs.getDate("correction_date"));
            return departmentReportPeriodJournalItem;
        }
    };

    /**
     * Маппер для {@link CorrectionDateRowMapper} представления значений из {@link ResultSet}
     * в пару значений Pair<reportPeriodId, correctionDate>
     */
    private class CorrectionDateRowMapper implements RowMapper<Pair<Integer, Date>> {
        @Override
        public Pair<Integer, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
            int reportPeriodId = rs.getInt("report_period_id");
            Date correctionDate = rs.getDate("correction_date");
            return new Pair<>(reportPeriodId, correctionDate);
        }
    }

    /**
     * Формирует sql выражение where с параметрами из фильтра
     *
     * @param filter - фильтр
     * @return строку с условиями выборки where
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
            causeList.add("drp.report_period_id = " + filter.getReportPeriod().getId());
        }

        if (causeList.isEmpty()) {
            return "";
        }

        return " where " + StringUtils.join(causeList, " and ");
    }

    @Override
    public List<DepartmentReportPeriod> fetchAllByFilter(final DepartmentReportPeriodFilter filter) {
        return (List<DepartmentReportPeriod>) exequteFetchByFilter(filter, mapper);
    }

    /**
     * Выполняет запрос в БД  с фильтрацией и формирует список объектов по указанному мапперу
     *
     * @param filter - фильтр
     * @param mapper - маппер
     * @return список объектов {@link DepartmentReportPeriod} или {@link DepartmentReportPeriodJournalItem}, в зависимости от маппера
     */
    private List<?> exequteFetchByFilter(final DepartmentReportPeriodFilter filter, RowMapper<?> mapper) {
        try {
            return getNamedParameterJdbcTemplate().query(String.format(QUERY_TEMPLATE_COMPOSITE_SORT, makeSqlWhereClause(filter)),
                    new HashMap<String, Object>(2) {{
                        put("yearStart", filter.getYearStart());
                        put("yearEnd", filter.getYearEnd());
                    }}, mapper);
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }


    @Override
    public List<Integer> fetchAllIdsByFilter(final DepartmentReportPeriodFilter filter) {
        try {
            return getNamedParameterJdbcTemplate().queryForList(
                    String.format(QUERY_TEMPLATE_COMPOSITE_SORT_ID, makeSqlWhereClause(filter)),
                    new HashMap<String, Object>(2) {{
                        put("yearStart", filter.getYearStart());
                        put("yearEnd", filter.getYearEnd());
                    }}, Integer.class);
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentReportPeriodJournalItem> fetchJournalItemByFilter(final DepartmentReportPeriodFilter filter) {
        return (List<DepartmentReportPeriodJournalItem>) exequteFetchByFilter(filter, mapperJournalItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#departmentReportPeriod.id")
    public void create(DepartmentReportPeriod departmentReportPeriod) {

        getJdbcTemplate()
                .update("insert into DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, " +
                                "CORRECTION_DATE) values (seq_department_report_period.nextval, ?, ?, ?, ?)",
                        departmentReportPeriod.getDepartmentId(),
                        departmentReportPeriod.getReportPeriod().getId(),
                        departmentReportPeriod.isActive(),
                        departmentReportPeriod.getCorrectionDate());
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#id")
    public void updateActive(int id, boolean active) {
        getJdbcTemplate().update(
                "update department_report_period set is_active = ? where id = ?",
                new Object[]{active ? 1 : 0, id},
                new int[]{Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void updateActive(final List<Integer> ids, final Integer report_period_id, final boolean active) {
        try {
            getJdbcTemplate().batchUpdate("update department_report_period set is_active = ? where report_period_id = ? AND id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, active ? 1 : 0);
                    ps.setInt(2, report_period_id);
                    ps.setInt(3, ids.get(i));
                }

                @Override
                public int getBatchSize() {
                    return ids.size();
                }
            });
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }


    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, key = "#id")
    @Deprecated
    public void delete(Integer id) {
        try {
            getJdbcTemplate().update(
                    "delete from department_report_period where id = ?",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
            );
        } catch (DataAccessException e) {
            LOG.error("", e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConstants.DEPARTMENT_REPORT_PERIOD, allEntries = true)
    public void delete(final List<Integer> ids) {
        try {
            getJdbcTemplate().batchUpdate("delete from department_report_period where id = ?",
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
        } catch (DataAccessException e) {
            LOG.error("", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistForDepartment(int departmentId, int reportPeriodId) {
        Integer count = getJdbcTemplate().queryForObject(
                "select count(*) from department_report_period where department_id = ? and report_period_id = ?",
                new Object[]{departmentId, reportPeriodId},
                new int[]{Types.NUMERIC, Types.NUMERIC},
                Integer.class
        );
        return count != 0;
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchLast(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "from " +
                            "department_report_period drp, " +
                            "(select max(correction_date) as correction_date, department_id, report_period_id " +
                            "from department_report_period " +
                            "where department_id = ? and report_period_id = ? " +
                            "group by department_id, report_period_id) m " +
                            "where drp.department_id = m.department_id " +
                            "and drp.report_period_id = m.report_period_id " +
                            "and (drp.correction_date = m.correction_date or (m.correction_date is null " +
                            "and drp.correction_date is null))",
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
            return getNamedParameterJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date from department_report_period drp " +
                            "where drp.REPORT_PERIOD_ID = :reportPeriodId and drp.DEPARTMENT_ID = :departmentId and " +
                            "drp.CORRECTION_DATE in (select max(CORRECTION_DATE) from department_report_period where " +
                            "REPORT_PERIOD_ID = :reportPeriodId and DEPARTMENT_ID = :departmentId and CORRECTION_DATE " +
                            "not in (select max(CORRECTION_DATE) from department_report_period where " +
                            "REPORT_PERIOD_ID = :reportPeriodId and DEPARTMENT_ID = :departmentId))",
                    params, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentReportPeriod fetchFirst(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.correction_date " +
                            "from " +
                            "department_report_period drp " +
                            "where drp.department_id = ? " +
                            "and drp.report_period_id = ? " +
                            "and drp.correction_date is null",
                    new Object[]{departmentId, reportPeriodId}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer fetchCorrectionNumber(int id) {
        try {
            return getJdbcTemplate().queryForObject(
                    "select num from (\n" +
                            "select id, correction_date as corr_date,\n" +
                            "row_number() over(partition by report_period_id, department_id order by correction_date nulls first) - 1 as num\n" +
                            "from department_report_period drp) where id = ?",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
                    , Integer.class
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkExistLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate) {
        try {
            return getJdbcTemplate().
                    queryForObject(
                            "select count(*) from department_report_period drp where " +
                                    "drp.DEPARTMENT_ID = ? and drp.report_period_id = ? and drp.CORRECTION_DATE > ?",
                            new Object[]{departmentId, reportPeriodId, correctionDate}, Integer.class) > 0;
        } catch (DataAccessException e) {
            LOG.error("", e);
            throw new DaoException("", e);
        }
    }


    @Override
    @Transactional(readOnly = true)
    @Deprecated
    public Map<Integer, List<Date>> fetchCorrectionDateListByReportPeriod(final Collection<Integer> reportPeriodIdList) {
        Map<Integer, List<Date>> retVal = new HashMap<>();
        if (reportPeriodIdList == null) {
            return retVal;
        }
        try {
            MapSqlParameterSource source = new MapSqlParameterSource();
            List<Pair<Integer, Date>> list = getNamedParameterJdbcTemplate().query("select distinct report_period_id, " +
                            "correction_date " +
                            "from department_report_period where " +
                            SqlUtils.transformToSqlInStatement("report_period_id", reportPeriodIdList) + " and correction_date is not null",
                    source, new CorrectionDateRowMapper());

            for (Pair<Integer, Date> pair : list) {
                if (!retVal.containsKey(pair.getFirst())) {
                    retVal.put(pair.getFirst(), new LinkedList<Date>());
                }
                retVal.get(pair.getFirst()).add(pair.getSecond());
            }
            return retVal;

        } catch (EmptyResultDataAccessException e) {
            return retVal;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> fetchIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId) {
        String query = "select drp.id from department_report_period drp \n" +
                "join department_report_period drp2 on drp2.id = :departmentReportPeriodId\n" +
                "where drp.department_id in (select id from department where type = :departmentType) \n" +
                "and drp.report_period_id = drp2.report_period_id and (drp.correction_date is null and drp2.correction_date is null or drp.correction_date = drp2.correction_date)";
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
            return getJdbcTemplate().queryForObject(QUERY_TEMPLATE_SIMPLE + " where id = ?", new Object[]{id}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}