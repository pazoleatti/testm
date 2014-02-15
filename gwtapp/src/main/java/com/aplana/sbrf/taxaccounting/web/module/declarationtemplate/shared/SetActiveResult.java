package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class SetActiveResult implements Result {
    private String uuid;
    private boolean isSetActiveSuccessfully;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isSetStatusSuccessfully() {
        return isSetActiveSuccessfully;
    }

    public void setIsSetActiveSuccessfully(boolean isSetActiveSuccessfully) {
        this.isSetActiveSuccessfully = isSetActiveSuccessfully;
    }

}
