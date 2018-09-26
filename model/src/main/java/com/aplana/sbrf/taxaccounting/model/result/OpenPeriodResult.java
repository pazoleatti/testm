package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Результат открытия периода
 */
@Getter
@Setter
@NoArgsConstructor
public class OpenPeriodResult {
    private String error;
    private String uuid;

    public OpenPeriodResult(String uuid) {
        this.uuid = uuid;
    }

    public OpenPeriodResult error(String error) {
        this.error = error;
        return this;
    }
}
