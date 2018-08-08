package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат открытия периода
 */
public class OpenPeriodResult {
    private String error;
    private String uuid;

    public OpenPeriodResult() {
    }

    public OpenPeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public OpenPeriodResult error(String error) {
        this.error = error;
        return this;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
