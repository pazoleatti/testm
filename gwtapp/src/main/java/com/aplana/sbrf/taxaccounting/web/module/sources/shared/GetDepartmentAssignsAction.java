package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.SourcesSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDepartmentAssignsAction extends UnsecuredActionImpl<GetDepartmentAssignsResult> implements ActionName {
    private int departmentId;
    private TaxType taxType;
    private boolean isForm = true;
    private PeriodsInterval periodsInterval;
    private DepartmentAssign selectedLeft;
    private SourcesSearchOrdering ordering;
    private boolean ascSorting;
    /** Назначение источников или приемников? */
    private SourceMode mode;

    public SourceMode getMode() {
        return mode;
    }

    public void setMode(SourceMode mode) {
        this.mode = mode;
    }

    public DepartmentAssign getSelectedLeft() {
        return selectedLeft;
    }

    public void setSelectedLeft(DepartmentAssign selectedLeft) {
        this.selectedLeft = selectedLeft;
    }

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    public SourcesSearchOrdering getOrdering() {
        return ordering;
    }

    public void setOrdering(SourcesSearchOrdering ordering) {
        this.ordering = ordering;
    }

    public boolean isAscSorting() {
        return ascSorting;
    }

    public void setAscSorting(boolean ascSorting) {
        this.ascSorting = ascSorting;
    }

    @Override
    public String getName() {
        return "Получение списка назначеных департаменту типов налоговых форм";
    }
}
