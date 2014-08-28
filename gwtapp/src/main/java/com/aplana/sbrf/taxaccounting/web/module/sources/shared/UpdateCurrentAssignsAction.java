package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

public class UpdateCurrentAssignsAction extends UnsecuredActionImpl<UpdateCurrentAssignsResult> {
    private DepartmentAssign departmentAssign;
    /** Признак того, что идет обработка назначений источников для деклараций */
    private boolean isDeclaration;
    /** Назначение источников или приемников? */
    private SourceMode mode;
    private PeriodsInterval newPeriodsInterval;
    private CurrentAssign currentAssign;

    private Date oldDateFrom;
    private Date oldDateTo;
    private TaxType taxType;

    public Date getOldDateFrom() {
        return oldDateFrom;
    }

    public void setOldDateFrom(Date oldDateFrom) {
        this.oldDateFrom = oldDateFrom;
    }

    public Date getOldDateTo() {
        return oldDateTo;
    }

    public void setOldDateTo(Date oldDateTo) {
        this.oldDateTo = oldDateTo;
    }

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

    public CurrentAssign getCurrentAssign() {
        return currentAssign;
    }

    public void setCurrentAssign(CurrentAssign currentAssign) {
        this.currentAssign = currentAssign;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }
}
