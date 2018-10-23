package com.aplana.sbrf.taxaccounting.model.builder;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxPeriod;

import java.util.Date;

public class DepartmentReportPeriodBuidler {
    private DepartmentReportPeriod departmentReportPeriod = new DepartmentReportPeriod();

    public DepartmentReportPeriodBuidler() {
        TaxPeriod taxPeriod = new TaxPeriod();
        ReportPeriod reportPeriod = new ReportPeriod();
        reportPeriod.setTaxPeriod(taxPeriod);
        departmentReportPeriod.setReportPeriod(reportPeriod);
    }
    
    public DepartmentReportPeriodBuidler reportPeriodId(Integer id) {
        departmentReportPeriod.getReportPeriod().setId(id);
        return this;
    }

    public DepartmentReportPeriodBuidler taxPeriodId(Integer id) {
        departmentReportPeriod.getReportPeriod().getTaxPeriod().setId(id);
        return this;
    }

    public DepartmentReportPeriodBuidler reportPeriodName(String name) {
        departmentReportPeriod.getReportPeriod().setName(name);
        return this;
    }

    public DepartmentReportPeriodBuidler correctionDate(Date correctionDate) {
        departmentReportPeriod.setCorrectionDate(correctionDate);
        return this;
    }

    public DepartmentReportPeriodBuidler department(int departmentId) {
        departmentReportPeriod.setDepartmentId(departmentId);
        return this;
    }

    public DepartmentReportPeriodBuidler active(boolean active) {
        departmentReportPeriod.setIsActive(active);
        return this;
    }

    public DepartmentReportPeriodBuidler year(int year) {
        departmentReportPeriod.getReportPeriod().getTaxPeriod().setYear(year);
        return this;
    }

    public DepartmentReportPeriodBuidler dictTaxPeriodId(long dictTaxPeriodId) {
        departmentReportPeriod.getReportPeriod().setDictTaxPeriodId(dictTaxPeriodId);
        return this;
    }

    public DepartmentReportPeriodBuidler but() {
        return new DepartmentReportPeriodBuidler()
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
