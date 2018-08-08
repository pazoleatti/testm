package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат удаления периода
 */
public class DeletePeriodResult {
    private String error;
    private String uuid;

    public DeletePeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public DeletePeriodResult error(String error) {
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
