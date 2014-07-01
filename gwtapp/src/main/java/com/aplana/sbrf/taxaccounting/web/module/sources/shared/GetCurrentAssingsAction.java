package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetCurrentAssingsAction extends UnsecuredActionImpl<GetCurrentSourcesResult> implements ActionName {

    private int departmentId;
    private int typeId;
    private FormDataKind kind;
    private boolean isForm = true;
    private PeriodsInterval periodsInterval;

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public FormDataKind getKind() {
        return kind;
    }

    public void setKind(FormDataKind kind) {
        this.kind = kind;
    }

    public boolean isForm() {
        return isForm;
    }

    public void setForm(boolean isForm) {
        this.isForm = isForm;
    }

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }

    @Override
    public String getName() {
        return "Получение списка, текущих назначений источнику/приемнику";
    }
}
