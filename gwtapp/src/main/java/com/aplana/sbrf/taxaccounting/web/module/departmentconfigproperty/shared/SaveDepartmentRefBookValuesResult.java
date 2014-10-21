package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.Result;

public class SaveDepartmentRefBookValuesResult implements Result {
    private String uuid;
    private boolean hasFatalError = false;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHasFatalError() {
        return hasFatalError;
    }

    public void setHasFatalError(boolean hasFatalError) {
        this.hasFatalError = hasFatalError;
    }
}
