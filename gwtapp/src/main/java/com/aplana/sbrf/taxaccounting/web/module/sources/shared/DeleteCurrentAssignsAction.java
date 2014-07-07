package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.model.source.SourcePair;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.CurrentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;
import java.util.Set;

public class DeleteCurrentAssignsAction  extends UnsecuredActionImpl<DeleteCurrentAssignsResult> {
    private Set<CurrentAssign> currentAssigns;
    private DepartmentAssign departmentAssign;
    /** Признак того, что идет обработка назначений источников для деклараций */
    private boolean isDeclaration;
    /** Назначение источников или приемников? */
    private SourceMode mode;
    private PeriodsInterval periodsInterval;

    public Set<CurrentAssign> getCurrentAssigns() {
        return currentAssigns;
    }

    public void setCurrentAssigns(Set<CurrentAssign> currentAssigns) {
        this.currentAssigns = currentAssigns;
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

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }
}
