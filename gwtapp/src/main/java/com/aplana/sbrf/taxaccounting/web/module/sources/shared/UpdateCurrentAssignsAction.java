package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.Set;

public class UpdateCurrentAssignsAction extends UnsecuredActionImpl<UpdateCurrentAssignsResult> {
    private DepartmentAssign departmentAssign;
    /** Признак того, что идет обработка назначений источников для деклараций */
    private boolean isDeclaration;
    /** Назначение источников или приемников? */
    private SourceMode mode;
    private PeriodsInterval newPeriodsInterval;
    private Set<CurrentAssign> currentAssigns;

    private TaxType taxType;
    /** Подразделение-слева */
    private Integer leftDepartmentId;

    public DepartmentAssign getDepartmentAssign() {
        return departmentAssign;
    }

    public void setDepartmentAssign(DepartmentAssign departmentAssign) {
        this.departmentAssign = departmentAssign;
    }

    public boolean isDeclaration() {
        return isDeclaration;
    }

    public void setDeclaration(boolean declaration) {
        isDeclaration = declaration;
    }

    public SourceMode getMode() {
        return mode;
    }

    public void setMode(SourceMode mode) {
        this.mode = mode;
    }

    public PeriodsInterval getNewPeriodsInterval() {
        return newPeriodsInterval;
    }

    public void setNewPeriodsInterval(PeriodsInterval newPeriodsInterval) {
        this.newPeriodsInterval = newPeriodsInterval;
    }

    public Set<CurrentAssign> getCurrentAssigns() {
        return currentAssigns;
    }

    public void setCurrentAssigns(Set<CurrentAssign> currentAssigns) {
        this.currentAssigns = currentAssigns;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getLeftDepartmentId() {
        return leftDepartmentId;
    }

    public void setLeftDepartmentId(Integer leftDepartmentId) {
        this.leftDepartmentId = leftDepartmentId;
    }
}
