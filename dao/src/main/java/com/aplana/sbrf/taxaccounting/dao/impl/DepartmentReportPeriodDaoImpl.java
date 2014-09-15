package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.api.DepartmentReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.api.ReportPeriodDao;
import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Repository
@Transactional(readOnly = true)
public class DepartmentReportPeriodDaoImpl extends AbstractDao implements DepartmentReportPeriodDao {

    @Autowired
    private ReportPeriodDao reportPeriodDao;

    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

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

    private static final String QUERY_TEMPLATE_COMPOSITE = "select drp.id, drp.department_id, drp.report_period_id, " +
            "drp.is_active, drp.is_balance_period, drp.correction_date from department_report_period drp " +
            "join report_period rp on drp.report_period_id = rp.id " +
            "join tax_period tp on rp.tax_period_id = tp.id";

    private String getFilterString(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        if (departmentReportPeriodFilter == null) {
            return "";
        }

        List<String> causeList = new LinkedList<String>();

        if (departmentReportPeriodFilter.isCorrection() != null) {
            causeList.add("drp.correction_date is " + (departmentReportPeriodFilter.isCorrection() ? " not " : "") + " null");
        }

        if (departmentReportPeriodFilter.isBalance() != null) {
            causeList.add("drp.is_balance_period " + (departmentReportPeriodFilter.isBalance() ? "<>" : "=") + " 0");
        }

        if (departmentReportPeriodFilter.isActive() != null) {
            causeList.add("drp.is_active " + (departmentReportPeriodFilter.isActive() ? "<>" : "=") + " 0");
        }

        if (departmentReportPeriodFilter.getCorrectionDate() != null) {
            causeList.add("drp.correction_date = to_date('" +
                    SIMPLE_DATE_FORMAT.format(departmentReportPeriodFilter.getCorrectionDate()) +
                    "', 'DD.MM.YYYY')");
        }

        if (departmentReportPeriodFilter.getDepartmentIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.department_id",
                    departmentReportPeriodFilter.getDepartmentIdList()));
        }

        if (departmentReportPeriodFilter.getReportPeriodIdList() != null) {
            causeList.add(SqlUtils.transformToSqlInStatement("drp.report_period_id",
                    departmentReportPeriodFilter.getReportPeriodIdList()));
        }

        if (departmentReportPeriodFilter.getTaxTypeList() != null) {
            causeList.add("tp.tax_type in " +
                    SqlUtils.transformTaxTypeToSqlInStatement(departmentReportPeriodFilter.getTaxTypeList()));
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
    public List<DepartmentReportPeriod> getListByFilter(DepartmentReportPeriodFilter departmentReportPeriodFilter) {
        return getNamedParameterJdbcTemplate().query(QUERY_TEMPLATE_COMPOSITE +
                getFilterString(departmentReportPeriodFilter), (Map) null, mapper);
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
    public void updateActive(int id, boolean active) {
        getJdbcTemplate().update(
                "update department_report_period set is_active = ? where id = ?",
                new Object[]{active, id},
                new int[]{Types.BOOLEAN, Types.NUMERIC}
        );
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
        getJdbcTemplate().update(
                "update department_report_period set is_balance_period = ? where id = ?",
                new Object[]{isBalance, id},
                new int[]{Types.BOOLEAN, Types.NUMERIC}
        );
    }

    @Override
    public void delete(int id) {
        getJdbcTemplate().update(
                "delete from department_report_period where id = ?",
                new Object[]{id},
                new int[]{Types.NUMERIC}
        );
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
    public Integer getCorrectionNumber(int id) {
        try {
            return getJdbcTemplate().queryForInt(
                    "select num from " +
                            "(select rownum-1 as num, drp1.id as id1, drp2.id as id2 " +
                            "from department_report_period drp1, department_report_period drp2 " +
                            "where drp1.department_id = drp2.department_id " +
                            "and drp1.report_period_id = drp2.report_period_id " +
                            "and drp2.id = ? " +
                            "order by  drp1.department_id,  drp1.report_period_id, drp1.correction_date nulls first) " +
                            "where id1 = id2",
                    new Object[]{id},
                    new int[]{Types.NUMERIC}
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
