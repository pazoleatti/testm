package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookColumn;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetBookerStatementsResult implements Result {
    private static final long serialVersionUID = -1550562671614416175L;
    private int totalCount;
    private List<RefBookDataRow> dataRows;
    private List<RefBookColumn> columns;

    /** Идентификаторы записей. Используются только при удалении */
    private List<Long> uniqueRecordIds;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<RefBookDataRow> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<RefBookDataRow> dataRows) {
        this.dataRows = dataRows;
    }

    public void setColumns(List<RefBookColumn> columns) {
        this.columns = columns;
    }

    public List<RefBookColumn> getColumns() {
        return columns;
    }

    public List<Long> getUniqueRecordIds() {
        return uniqueRecordIds;
    }

    public void setUniqueRecordIds(List<Long> uniqueRecordIds) {
        this.uniqueRecordIds = uniqueRecordIds;
    }
}
