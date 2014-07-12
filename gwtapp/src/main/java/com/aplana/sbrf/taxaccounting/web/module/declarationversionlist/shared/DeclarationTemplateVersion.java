package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.shared;

import java.io.Serializable;

/**
 * User: avanteev
 */
public class DeclarationTemplateVersion implements Serializable {
    private String dtId;
    private String typeName;
    private String actualBeginVersionDate;
    private String actualEndVersionDate;

    public String getDtId() {
        return dtId;
    }

    public void setDtId(String dtId) {
        this.dtId = dtId;
    }

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
}
