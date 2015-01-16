package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

/**
 * User: avanteev
 */
public class GetLastVersionHierarchyResult implements Result {
    RefBookDataRow dataRow;

    public RefBookDataRow getDataRow() {
        return dataRow;
    }

    public void setDataRow(RefBookDataRow dataRow) {
        this.dataRow = dataRow;
    }
}
