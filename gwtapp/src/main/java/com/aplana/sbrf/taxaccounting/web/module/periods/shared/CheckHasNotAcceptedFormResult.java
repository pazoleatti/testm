package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckHasNotAcceptedFormResult implements Result {
    boolean hasNotAcceptedForms;
    String uuid;

    public boolean hasNotAcceptedForms() {
        return hasNotAcceptedForms;
    }

    public void setHasNotAcceptedForms(boolean hasNotAcceptedForms) {
        this.hasNotAcceptedForms = hasNotAcceptedForms;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
