package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodInfo;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class UpdateCurrentAssignsAction extends UnsecuredActionImpl<UpdateCurrentAssignsResult> {
    private Long departmentAssignId;
    private List<Long> rightDepartmentAssignIds;
    private PeriodInfo periodFrom;
    private PeriodInfo periodTo;
    private int yearFrom;
    private int yearTo;
    private boolean isDeclaration;

    public PeriodInfo getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(PeriodInfo periodFrom) {
        this.periodFrom = periodFrom;
    }

    public PeriodInfo getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(PeriodInfo periodTo) {
        this.periodTo = periodTo;
    }

    public int getYearFrom() {
        return yearFrom;
    }

    public void setYearFrom(int yearFrom) {
        this.yearFrom = yearFrom;
    }

    public int getYearTo() {
        return yearTo;
    }

    public void setYearTo(int yearTo) {
        this.yearTo = yearTo;
    }

    public Long getDepartmentAssignId() {
        return departmentAssignId;
    }

    public void setDepartmentAssignId(Long departmentAssignId) {
        this.departmentAssignId = departmentAssignId;
    }

    public List<Long> getRightDepartmentAssignIds() {
        return rightDepartmentAssignIds;
    }

    public void setRightDepartmentAssignIds(List<Long> rightDepartmentAssignIds) {
        this.rightDepartmentAssignIds = rightDepartmentAssignIds;
    }

    public boolean isDeclaration() {
        return isDeclaration;
    }

    public void setDeclaration(boolean declaration) {
        isDeclaration = declaration;
    }
}
