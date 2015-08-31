package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class CreateFormData extends UnsecuredActionImpl<CreateFormDataResult> implements ActionName {

    private Integer formDataTypeId;

    private Integer reportPeriodId;

    private Integer departmentId;

    private Integer formDataKindId;

    private Integer monthId;
    private Integer comparativePeriodId;
    private boolean accruing;

    public Integer getFormDataTypeId() {
        return formDataTypeId;
    }


    public void setFormDataTypeId(Integer formDataTypeId) {
        this.formDataTypeId = formDataTypeId;
    }


    public Integer getReportPeriodId() {
        return reportPeriodId;
    }


    public void setReportPeriodId(Integer reportPeriodId) {
        this.reportPeriodId = reportPeriodId;
    }


    public Integer getDepartmentId() {
        return departmentId;
    }


    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }


    public Integer getFormDataKindId() {
        return formDataKindId;
    }


    public void setFormDataKindId(Integer formDataKindId) {
        this.formDataKindId = formDataKindId;
    }

    public Integer getMonthId() {
        return monthId;
    }

    public void setMonthId(Integer monthId) {
        this.monthId = monthId;
    }

    @Override
    public String getName() {
        return "Создание налоговой формы";
    }

    public void setComparativePeriodId(Integer comparativePeriodId) {
        this.comparativePeriodId = comparativePeriodId;
    }

    public Integer getComparativePeriodId() {
        return comparativePeriodId;
    }

    public void setAccruing(boolean accruing) {
        this.accruing = accruing;
    }

    public boolean isAccruing() {
        return accruing;
    }
}
