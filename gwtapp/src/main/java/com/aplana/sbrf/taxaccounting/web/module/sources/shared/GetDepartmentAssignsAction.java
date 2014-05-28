package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class GetDepartmentAssignsAction extends UnsecuredActionImpl<GetDepartmentAssignsResult> implements ActionName {
    private int departmentId;
    private TaxType taxType;
    private boolean isForm = true;

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

    @Override
    public String getName() {
        return "Получение списка, назначеных департименту, " + (isForm ? "типов (налоговых) форм" : "деклараций/уведомлений");
    }
}
