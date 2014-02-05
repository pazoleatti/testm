package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import java.io.Serializable;

/**
 * User: avanteev
 * Модель для отображения списка макетов деклраций
 */
public class DeclarationTypeTemplate implements Serializable {
    private int typeId;
    private String typeName;
    private int versionCount;

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getVersionCount() {
        return versionCount;
    }

    public void setVersionCount(int versionCount) {
        this.versionCount = versionCount;
    }
}
