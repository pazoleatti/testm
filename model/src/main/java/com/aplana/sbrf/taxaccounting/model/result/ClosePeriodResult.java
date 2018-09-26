package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.Setter;

/**
 * Результат закрытия периода
 */
@Getter
@Setter
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
}
