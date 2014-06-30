package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class EditFormTypeNameAction extends UnsecuredActionImpl<EditFormTypeNameResult> {
    int formTypeId;
    String newFormTypeName;

    public int getFormTypeId() {
        return formTypeId;
    }

    public void setFormTypeId(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    public String getNewFormTypeName() {
        return newFormTypeName;
    }

    public void setNewFormTypeName(String newFormTypeName) {
        this.newFormTypeName = newFormTypeName;
    }
}
