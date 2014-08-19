package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class HasManualVersionResult implements Result {

    boolean hasManualVersion;

    public boolean isHasManualVersion() {
        return hasManualVersion;
    }

    public void setHasManualVersion(boolean hasManualVersion) {
        this.hasManualVersion = hasManualVersion;
    }
}
