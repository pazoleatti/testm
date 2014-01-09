package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetRefBookRecordVersionResult implements Result {
    List<RefBookDataRow> dataRows;
    int totalCount;

    public List<RefBookDataRow> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<RefBookDataRow> dataRows) {
        this.dataRows = dataRows;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
