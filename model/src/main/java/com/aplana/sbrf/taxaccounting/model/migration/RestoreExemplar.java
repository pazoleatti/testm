package com.aplana.sbrf.taxaccounting.model.migration;

import com.aplana.sbrf.taxaccounting.model.migration.enums.Periodity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alexander Ivanov
 * @date: 26.08.13
 */
public class RestoreExemplar implements Serializable {

    private Periodity periodity;
    private Date beginDate;
    private Date endDate;

    private Integer formTemplateId;
    private String systemCode;

    private Integer departmentId;

    private Integer taxPeriod;
    private Integer dictTaxPeriodId;

    public Periodity getPeriodity() {
        return periodity;
    }

    public void setPeriodity(Periodity periodity) {
        this.periodity = periodity;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(Integer formTemplateId) {
        this.formTemplateId = formTemplateId;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getTaxPeriod() {
        return taxPeriod;
    }

    public void setTaxPeriod(Integer taxPeriod) {
        this.taxPeriod = taxPeriod;
    }

    public Integer getDictTaxPeriodId() {
        return dictTaxPeriodId;
    }

    public void setDictTaxPeriodId(Integer dictTaxPeriodId) {
        this.dictTaxPeriodId = dictTaxPeriodId;
    }
}
