package com.aplana.sbrf.taxaccounting.model.action;

import java.util.List;

public class FetchDeclarationTypeAction {

    private List<Long> formDataKindIdList;

    private Integer departmentId;

    private Integer periodId;

    public List<Long> getFormDataKindIdList() {
        return formDataKindIdList;
    }

    public void setFormDataKindIdList(List<Long> formDataKindIdList) {
        this.formDataKindIdList = formDataKindIdList;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getPeriodId() {
        return periodId;
    }

    public void setPeriodId(Integer periodId) {
        this.periodId = periodId;
    }
}
