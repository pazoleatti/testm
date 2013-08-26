package com.aplana.sbrf.taxaccounting.dao.mapper;

import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: ekuvshinov
 */
public class ReportPeriodMapper implements RowMapper<ReportPeriod> {
    @Override
    public ReportPeriod mapRow(ResultSet rs, int index) throws SQLException {
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setId(rs.getInt("id"));
        reportPeriod.setName(rs.getString("name"));
        reportPeriod.setActive(rs.getBoolean("is_active"));
        reportPeriod.setMonths(rs.getInt("months"));
        reportPeriod.setTaxPeriodId(rs.getInt("tax_period_id"));
        reportPeriod.setOrder(rs.getInt("ord"));
        reportPeriod.setBalancePeriod(rs.getBoolean("is_balance_period"));
        reportPeriod.setDepartmentId(rs.getLong("department_id"));
        reportPeriod.setDictTaxPeriodId(rs.getInt("dict_tax_period_id"));
        return reportPeriod;
    }
}
