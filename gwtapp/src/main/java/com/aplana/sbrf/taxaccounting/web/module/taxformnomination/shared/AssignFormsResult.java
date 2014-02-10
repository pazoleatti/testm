package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;

import com.gwtplatform.dispatch.shared.Result;

public class AssignFormsResult implements Result {

    private static final long serialVersionUID = 3146133889534657644L;

    private boolean issetRelations;
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isIssetRelations() {
        return issetRelations;
    }

    public void setIssetRelations(boolean issetRelations) {
        this.issetRelations = issetRelations;
    }

}
