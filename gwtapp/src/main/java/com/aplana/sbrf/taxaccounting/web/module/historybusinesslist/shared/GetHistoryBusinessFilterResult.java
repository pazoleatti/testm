package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class GetHistoryBusinessFilterResult implements Result {
    private LogSystemFilterAvailableValues availableValues;

    public LogSystemFilterAvailableValues getAvailableValues() {
        return availableValues;
    }

    public void setAvailableValues(LogSystemFilterAvailableValues availableValues) {
        this.availableValues = availableValues;
    }
}
