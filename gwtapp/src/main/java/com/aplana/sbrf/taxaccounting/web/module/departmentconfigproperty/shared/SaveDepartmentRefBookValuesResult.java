package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.shared;

import com.gwtplatform.dispatch.shared.Result;


public class SaveDepartmentRefBookValuesResult implements Result {
    private String errorMsg;

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public enum ERROR_TYPE {
        NONE,
        HAS_DUPLICATES,
        INCORRECT_FIELDS,
        COMMON_ERROR,
        SAVING_FAILED
    }

    private String uuid;
    private boolean hasFatalError = false;
    private ERROR_TYPE errorType = ERROR_TYPE.NONE;

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

    public ERROR_TYPE getErrorType() {
        return errorType;
    }

    public void setErrorType(ERROR_TYPE errorType) {
        this.errorType = errorType;
    }
}
