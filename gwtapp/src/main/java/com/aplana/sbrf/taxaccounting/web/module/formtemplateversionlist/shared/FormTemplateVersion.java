package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared;

import java.io.Serializable;

/**
 * User: avanteev
 */
public class FormTemplateVersion implements Serializable {

    private String formTemplateId;
    private String typeName;
    private String actualBeginVersionDate;
    private String actualEndVersionDate;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public String getActualBeginVersionDate() {
        return actualBeginVersionDate;
    }

    public void setActualBeginVersionDate(String actualBeginVersionDate) {
        this.actualBeginVersionDate = actualBeginVersionDate;
    }

    public String getActualEndVersionDate() {
        return actualEndVersionDate;
    }

    public void setActualEndVersionDate(String actualEndVersionDate) {
        this.actualEndVersionDate = actualEndVersionDate;
    }

    public String getFormTemplateId() {
        return formTemplateId;
    }

    public void setFormTemplateId(String formTemplateId) {
        this.formTemplateId = formTemplateId;
    }
}
