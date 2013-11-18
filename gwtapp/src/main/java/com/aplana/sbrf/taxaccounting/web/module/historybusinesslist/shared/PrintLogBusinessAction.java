package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.LogBusinessFilterValues;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * User: avanteev
 */
public class PrintLogBusinessAction extends UnsecuredActionImpl<PrintLogBusinessResult> implements ActionName {
    private LogBusinessFilterValues filterValues;

    public LogBusinessFilterValues getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(LogBusinessFilterValues filterValues) {
        this.filterValues = filterValues;
    }

    @Override
    public String getName() {
        return "";
    }
}
