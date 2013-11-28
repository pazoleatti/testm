package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.LogSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetHistoryBusinessListResult implements Result {
    private long totalCountOfRecords;
    private List<LogSearchResultItem> records;

    public long getTotalCountOfRecords() {
        return totalCountOfRecords;
    }

    public void setTotalCountOfRecords(long totalCountOfRecords) {
        this.totalCountOfRecords = totalCountOfRecords;
    }

    public List<LogSearchResultItem> getRecords() {
        return records;
    }

    public void setRecords(List<LogSearchResultItem> records) {
        this.records = records;
    }
}
