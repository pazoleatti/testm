package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDuplicatePersonResult implements Result {

	private List<RefBookAttribute> tableHeaders;
    private RefBookDataRow dataRow;
    private RefBookDataRow originalRow;
    private List<RefBookDataRow> duplicateRows;

    public List<RefBookAttribute> getTableHeaders() {
        return tableHeaders;
    }

    public void setTableHeaders(List<RefBookAttribute> tableHeaders) {
        this.tableHeaders = tableHeaders;
    }

    public RefBookDataRow getDataRow() {
        return dataRow;
    }

    public void setDataRow(RefBookDataRow dataRow) {
        this.dataRow = dataRow;
    }

    public RefBookDataRow getOriginalRow() {
        return originalRow;
    }

    public void setOriginalRow(RefBookDataRow originalRow) {
        this.originalRow = originalRow;
    }

    public List<RefBookDataRow> getDuplicateRows() {
        return duplicateRows;
    }

    public void setDuplicateRows(List<RefBookDataRow> duplicateRows) {
        this.duplicateRows = duplicateRows;
    }
}
