package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared;

import com.aplana.sbrf.taxaccounting.model.LogSystemSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * User: avanteev
 */
public class GetHistoryBusinessListResult implements Result {
    private long totalCountOfRecords;
    private List<LogSystemSearchResultItem> records;

    public long getTotalCountOfRecords() {
        return totalCountOfRecords;
    }

    public void setTotalCountOfRecords(long totalCountOfRecords) {
        this.totalCountOfRecords = totalCountOfRecords;
    }

    public List<LogSystemSearchResultItem> getRecords() {
        return records;
    }

    public void setRecords(List<LogSystemSearchResultItem> records) {
        this.records = records;
    }
}
