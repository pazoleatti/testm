package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.gwtplatform.dispatch.shared.Result;

public class UpdateNdflReferenceResult implements Result {
    Integer rowsUpdated;

    public Integer getRowsUpdated() {
        return rowsUpdated;
    }

    public void setRowsUpdated(Integer rowsUpdated) {
        this.rowsUpdated = rowsUpdated;
    }
}
