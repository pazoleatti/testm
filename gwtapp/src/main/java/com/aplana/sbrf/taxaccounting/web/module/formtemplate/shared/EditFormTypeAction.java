package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

public class EditFormTypeAction extends UnsecuredActionImpl<EditFormTypeResult> {
    int formTypeId;
    String newFormTypeName;
    String newFormTypeCode;

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

    public String getNewFormTypeCode() {
        return newFormTypeCode;
    }

    public void setNewFormTypeCode(String newFormTypeCode) {
        this.newFormTypeCode = newFormTypeCode;
    }
}
