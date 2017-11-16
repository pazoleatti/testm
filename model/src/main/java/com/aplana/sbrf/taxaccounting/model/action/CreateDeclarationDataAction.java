package com.aplana.sbrf.taxaccounting.model.action;

public class CreateDeclarationDataAction {

    private Long declarationTypeId;

    private Integer departmentId;

    private Integer periodId;

    private Boolean manuallyCreated = false;

    public Long getDeclarationTypeId() {
        return declarationTypeId;
    }

    public void setDeclarationTypeId(Long declarationTypeId) {
        this.declarationTypeId = declarationTypeId;
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

    public Boolean getManuallyCreated() {
        return manuallyCreated;
    }

    public void setManuallyCreated(Boolean manuallyCreated) {
        this.manuallyCreated = manuallyCreated;
    }
}
