package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.LogBusinessFilterValues;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class GetHistoryBusinessListAction extends UnsecuredActionImpl<GetHistoryBusinessListResult> {
    private LogBusinessFilterValues filterValues;

    public LogBusinessFilterValues getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(LogBusinessFilterValues filterValues) {
        this.filterValues = filterValues;
    }
}
