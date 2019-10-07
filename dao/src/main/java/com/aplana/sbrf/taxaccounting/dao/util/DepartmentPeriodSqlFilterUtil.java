package com.aplana.sbrf.taxaccounting.dao.util;

import com.aplana.sbrf.taxaccounting.dao.impl.util.SqlUtils;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

public class DepartmentPeriodSqlFilterUtil {

    private static final ThreadLocal<SimpleDateFormat> SIMPLE_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy");
        }
    };

    /**
     * Формирует sql выражение where с параметрами из фильтра
     *
     * @param filter фильтр
     * @return строка с условиями выборки where
     */
    public static String makeSqlWhereClause(DepartmentReportPeriodFilter filter) {
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

        if (filter.getDictTaxPeriodId() != null) {
            causeList.add("rp.dict_tax_period_id = " + filter.getDictTaxPeriodId());
        }

        if (causeList.isEmpty()) {
            return "";
        }

        return " where " + StringUtils.join(causeList, " and ");
    }
}
