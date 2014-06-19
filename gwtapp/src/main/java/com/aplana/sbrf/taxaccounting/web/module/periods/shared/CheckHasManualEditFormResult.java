package com.aplana.sbrf.taxaccounting.web.module.periods.shared;

import com.gwtplatform.dispatch.shared.Result;

public class CheckHasManualEditFormResult implements Result {
    boolean hasManualInputForms;
    String uuid;

    public boolean hasManualInputForms() {
        return hasManualInputForms;
    }

    public void setHasManualInputForms(boolean hasManualInputForms) {
        this.hasManualInputForms = hasManualInputForms;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
