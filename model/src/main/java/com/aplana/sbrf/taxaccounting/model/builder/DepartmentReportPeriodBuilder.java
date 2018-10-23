package com.aplana.sbrf.taxaccounting.model.builder;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

import java.util.Date;

public class DepartmentReportPeriodBuilder {
    private DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();

    public DepartmentReportPeriodBuilder() {
        TaxPeriod taxPeriod = new TaxPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(taxPeriod);
        departmentReportPeriod.setReportPeriod(reportPeriod);
    }
    
    public DepartmentReportPeriodBuilder reportPeriodId(Integer id) {
        departmentReportPeriod.getReportPeriod().setId(id);
        return this;
    }

    public DepartmentReportPeriodBuilder taxPeriodId(Integer id) {
        departmentReportPeriod.getReportPeriod().getTaxPeriod().setId(id);
        return this;
    }

    public DepartmentReportPeriodBuilder reportPeriodName(String name) {
        departmentReportPeriod.getReportPeriod().setName(name);
        return this;
    }

    public DepartmentReportPeriodBuilder correctionDate(Date correctionDate) {
        departmentReportPeriod.setCorrectionDate(correctionDate);
        return this;
    }

    public DepartmentReportPeriodBuilder department(int departmentId) {
        departmentReportPeriod.setDepartmentId(departmentId);
        return this;
    }

    public DepartmentReportPeriodBuilder active(boolean active) {
        departmentReportPeriod.setIsActive(active);
        return this;
    }

    public DepartmentReportPeriodBuilder year(int year) {
        departmentReportPeriod.getReportPeriod().getTaxPeriod().setYear(year);
        return this;
    }

    public DepartmentReportPeriodBuilder dictTaxPeriodId(long dictTaxPeriodId) {
        departmentReportPeriod.getReportPeriod().setDictTaxPeriodId(dictTaxPeriodId);
        return this;
    }

    public DepartmentReportPeriodBuilder but() {
        return new DepartmentReportPeriodBuilder()
                .reportPeriodId(departmentReportPeriod.getReportPeriod().getId())
                .taxPeriodId(departmentReportPeriod.getReportPeriod().getTaxPeriod().getId())
                .reportPeriodName(departmentReportPeriod.getReportPeriod().getName())
                .correctionDate(departmentReportPeriod.getCorrectionDate())
                .department(departmentReportPeriod.getDepartmentId())
                .active(departmentReportPeriod.isActive())
                .year(departmentReportPeriod.getReportPeriod().getTaxPeriod().getYear())
                .dictTaxPeriodId(departmentReportPeriod.getReportPeriod().getDictTaxPeriodId());
    }

    public DepartmentReportPeriod build() {
        return departmentReportPeriod;
    }
}
