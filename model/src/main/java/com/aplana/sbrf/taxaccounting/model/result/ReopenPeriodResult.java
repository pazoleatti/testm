package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат переоткрытия периода
 */
public class ReopenPeriodResult {
    private String error;
    private String uuid;

    public ReopenPeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public ReopenPeriodResult error(String error) {
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
