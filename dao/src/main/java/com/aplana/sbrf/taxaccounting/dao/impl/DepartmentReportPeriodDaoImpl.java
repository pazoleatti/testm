package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.exception.DaoException;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@Transactional(readOnly = true)
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

    private final RowMapper<DepartmentReportPeriod> mapper = new RowMapper<DepartmentReportPeriod>() {
        @Override
        public DepartmentReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
            DepartmentReportPeriod reportPeriod = new DepartmentReportPeriod();
            reportPeriod.setId(SqlUtils.getInteger(rs, "id"));
            reportPeriod.setDepartmentId(SqlUtils.getInteger(rs, "department_id"));
            reportPeriod.setReportPeriod(reportPeriodDao.get(SqlUtils.getInteger(rs, "report_period_id")));
            reportPeriod.setActive(!SqlUtils.getInteger(rs, "is_active").equals(0));
            reportPeriod.setBalance(!SqlUtils.getInteger(rs, "is_balance_period").equals(0));
            reportPeriod.setCorrectionDate(rs.getDate("correction_date"));
            return reportPeriod;
        }
    };

    private static final String QUERY_TEMPLATE_SIMPLE = "select id, department_id, report_period_id, is_active, " +
            "is_balance_period, correction_date from department_report_period";

    private static final String QUERY_TEMPLATE_COMPOSITE_SORT = "select drp.id, drp.department_id, drp.report_period_id, drp.is_active, drp.is_balance_period, drp.correction_date \n" +
            "          from \n" +
            "          department_report_period drp \n" +
            "          join report_period rp on drp.report_period_id = rp.id \n" +
            "          join tax_period tp on rp.tax_period_id = tp.id \n" +
            "            %s \n" +
            "            order by tp.year, drp.CORRECTION_DATE NULLS FIRST";

    private String getFilterString(DepartmentReportPeriodFilter filter) {
        if (filter == null) {
            return "";
        }

        List<String> causeList = new LinkedList<String>();

        if (filter.isCorrection() != null) {
            causeList.add("drp.correction_date is " + (filter.isCorrection() ? " not " : "") + " null");
        }

        if (filter.isBalance() != null) {
            causeList.add("drp.is_balance_period " + (filter.isBalance() ? "<>" : "=") + " 0");
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
        if (filter.getYearStart() != null || filter.getYearEnd() != null){
            causeList.add("(:yearStart is null or tp.year >= :yearStart) and (:yearEnd is null or tp.year <= :yearEnd)");
        }

        if (causeList.isEmpty()) {
            return "";
        }

        return " where " + StringUtils.join(causeList, " and ");
    }

    @Override
    public DepartmentReportPeriod get(int id) {
        try {
            return getJdbcTemplate().queryForObject(QUERY_TEMPLATE_SIMPLE + " where id = ?", new Object[]{id}, mapper);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<DepartmentReportPeriod> getListByFilter(final DepartmentReportPeriodFilter filter) {
        try {
            return getNamedParameterJdbcTemplate().query(String.format(QUERY_TEMPLATE_COMPOSITE_SORT, getFilterString(filter)),
                    new HashMap<String, Object>(2) {{
                        put("yearStart", filter.getYearStart());
                        put("yearEnd", filter.getYearEnd());
                    }}, mapper);
        } catch (DataAccessException e) {
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private static final String QUERY_TEMPLATE_COMPOSITE_SORT_ID =
            "select drp.id from department_report_period drp " +
                    "join report_period rp on drp.report_period_id = rp.id \n" +
                    "join tax_period tp on rp.tax_period_id = tp.id " +
                    " %s ";

    @Override
    public List<Integer> getListIdsByFilter(final DepartmentReportPeriodFilter filter) {
        try {
            return getNamedParameterJdbcTemplate().queryForList(
                    String.format(QUERY_TEMPLATE_COMPOSITE_SORT_ID, getFilterString(filter)),
                    new HashMap<String, Object>(2) {{
                        put("yearStart", filter.getYearStart());
                        put("yearEnd", filter.getYearEnd());
                    }}, Integer.class);
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public int save(DepartmentReportPeriod departmentReportPeriod) {
        Integer id = departmentReportPeriod.getId();
        if (id == null) {
            id = generateId("seq_department_report_period", Integer.class);
        }

        getJdbcTemplate()
                .update("insert into DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, " +
                        "IS_BALANCE_PERIOD, CORRECTION_DATE) values (?, ?, ?, ?, ?, ?)",
                        id,
                        departmentReportPeriod.getDepartmentId(),
                        departmentReportPeriod.getReportPeriod().getId(),
                        departmentReportPeriod.isActive(),
                        departmentReportPeriod.isBalance(),
                        departmentReportPeriod.getCorrectionDate());
        return id;
    }

    @Override
    @Transactional(readOnly = false)
    public void save(final DepartmentReportPeriod departmentReportPeriod, final List<Integer> departmentIds) {
        getJdbcTemplate()
                .batchUpdate("insert into DEPARTMENT_REPORT_PERIOD (ID, DEPARTMENT_ID, REPORT_PERIOD_ID, IS_ACTIVE, " +
                                "IS_BALANCE_PERIOD, CORRECTION_DATE) select seq_department_report_period.nextval, ?, ?, ?, ?, ? from dual",
                        new BatchPreparedStatementSetter() {
                            @Override
                            public void setValues(PreparedStatement ps, int i) throws SQLException {
                                ps.setInt(1, departmentIds.get(i));
                                ps.setInt(2, departmentReportPeriod.getReportPeriod().getId());
                                ps.setInt(3, departmentReportPeriod.isActive() ? 1 : 0);
                                ps.setInt(4, departmentReportPeriod.isBalance() ? 1 : 0);
                                ps.setDate(5, departmentReportPeriod.getCorrectionDate() == null ? null : new java.sql.Date(departmentReportPeriod.getCorrectionDate().getTime()));
                            }

                            @Override
                            public int getBatchSize() {
                                return departmentIds.size();
                            }
                        });

    }

    @Override
    public void updateActive(int id, boolean active, boolean isBalance) {
        getJdbcTemplate().update(
                "update department_report_period set is_active = ?, is_balance_period = ? where id = ?",
                new Object[]{active ? 1 : 0, isBalance ? 1 : 0, id},
                new int[]{Types.NUMERIC, Types.NUMERIC, Types.NUMERIC}
        );
    }

    @Override
    public void updateActive(final List<Integer> ids, final Integer report_period_id, final boolean active, final boolean isBalance) {
        try {
            getJdbcTemplate().batchUpdate("update department_report_period set is_active = ?, is_balance_period = ? where report_period_id = ? AND id = ?", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, active ? 1 : 0);
                    ps.setInt(2, isBalance ? 1 : 0);
                    ps.setInt(3, report_period_id);
                    ps.setInt(4, ids.get(i));
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
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void updateCorrectionDate(int id, Date correctionDate) {
        getJdbcTemplate().update(
                "update department_report_period set correction_date = ? where id = ?",
                new Object[]{correctionDate, id},
                new int[]{Types.DATE, Types.NUMERIC}
        );
    }

    @Override
    public void updateBalance(int id, boolean isBalance) {
        try {
            getJdbcTemplate().update(
                    "update department_report_period set is_balance_period = ? where id = ?",
                    new Object[]{isBalance ? 1: 0, id},
                    new int[]{Types.NUMERIC, Types.NUMERIC}
            );
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void updateBalance(final List<Integer> ids, final boolean isBalance) {
        try {
            getJdbcTemplate().batchUpdate("update department_report_period set is_balance_period = ? where id = ?",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, isBalance ? 1: 0);
                            ps.setInt(2, ids.get(i));
                        }

                        @Override
                        public int getBatchSize() {
                            return ids.size();
                        }
                    });
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    @Override
    public void delete(int id) {
        try {
            getJdbcTemplate().update(
                    "delete from department_report_period where id = ?",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
            );
        } catch (DataAccessException e){
			LOG.error("", e);
        }
    }

    @Override
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
        } catch (DataAccessException e){
			LOG.error("", e);
        }
    }

    @Override
    public boolean existForDepartment(int departmentId, int reportPeriodId) {
        Integer count = getJdbcTemplate().queryForInt(
                "select count(*) from department_report_period where department_id = ? and report_period_id = ?",
                new Object[]{departmentId, reportPeriodId},
                new int[]{Types.NUMERIC, Types.NUMERIC}
        );
        return count != 0;
    }

    @Override
    public DepartmentReportPeriod getLast(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                    "drp.is_active, drp.is_balance_period, drp.correction_date " +
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
    public DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId) {
        try {
            return getJdbcTemplate().queryForObject("select drp.id, drp.department_id, drp.report_period_id, " +
                            "drp.is_active, drp.is_balance_period, drp.correction_date " +
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
    public Integer getCorrectionNumber(int id) {
        try {
            return getJdbcTemplate().queryForInt(
                    "select num from (\n" +
                            "select id, correction_date as corr_date,\n" +
                            "row_number() over(partition by report_period_id, department_id order by correction_date nulls first) - 1 as num\n" +
                            "from department_report_period drp) where id = ?",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public boolean existLargeCorrection(int departmentId, int reportPeriodId, Date correctionDate) {
        try {
            return getJdbcTemplate().
                    queryForInt(
                            "select count(*) from department_report_period drp where " +
                                    "drp.DEPARTMENT_ID = ? and drp.report_period_id = ? and drp.CORRECTION_DATE > ?",
                            departmentId, reportPeriodId, correctionDate) > 0;
        } catch (DataAccessException e){
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }

    private class CorrectionDateRowMapper implements RowMapper<Pair<Integer, Date>> {
        @Override
        public Pair<Integer, Date> mapRow(ResultSet rs, int rowNum) throws SQLException {
            int reportPeriodId = rs.getInt("report_period_id");
            Date correctionDate = rs.getDate("correction_date");
            return new Pair<Integer, Date>(reportPeriodId, correctionDate);
        }
    }

    @Override
    public Map<Integer, List<Date>> getCorrectionDateListByReportPeriod(final Collection<Integer> reportPeriodIdList) {
        Map<Integer, List<Date>> retVal = new HashMap<Integer, List<Date>>();
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
    public List<DepartmentReportPeriod> getClosedForFormTemplate(final int formTemplateId) {
        try {
            return getNamedParameterJdbcTemplate().query("select drp.id, drp.department_id, drp.report_period_id, " +
                    "drp.is_active, drp.is_balance_period, drp.correction_date " +
                    "from department_report_period drp, form_data fd " +
                    "where drp.id = fd.department_report_period_id " +
                    "and drp.is_active = 0 and fd.form_template_id = :formTemplateId",
                    new HashMap<String, Object>(2) {{
                        put("formTemplateId", formTemplateId);
                    }}, mapper);
        } catch (DataAccessException e) {
			LOG.error("", e);
            throw new DaoException("", e);
        }
    }
}
