package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат закрытия периода
 */
public class ClosePeriodResult {
    private String error;

    private String uuid;

    private boolean isFatal = true;

    public ClosePeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public ClosePeriodResult error(String error) {
        this.error = error;
        return this;
    }

    public ClosePeriodResult fatal(boolean isFatal) {
        this.isFatal = isFatal;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isFatal() {
        return isFatal;
    }

    public void setFatal(boolean fatal) {
        isFatal = fatal;
    }
}
