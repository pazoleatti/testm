package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат закрытия периода
 */
public class ClosePeriodResult {
    private String uuid;

    private String warning;

    public ClosePeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public ClosePeriodResult(String uuid, String warning) {
        this.uuid = uuid;
        this.warning = warning;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }
}
