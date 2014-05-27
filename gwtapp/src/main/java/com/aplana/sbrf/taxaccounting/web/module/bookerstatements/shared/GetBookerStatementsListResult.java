package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.BookerStatementsSearchResultItem;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;
import java.util.Map;

/**
 * @author lhaziev
 */
public class GetBookerStatementsListResult implements Result {
    private static final long serialVersionUID = -2065195413561128168L;
    private int totalCount;
    private List<BookerStatementsSearchResultItem> dataRows;
    Map<Integer, String> departmentFullNames;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<BookerStatementsSearchResultItem> getDataRows() {
        return dataRows;
    }

    public void setDataRows(List<BookerStatementsSearchResultItem> dataRows) {
        this.dataRows = dataRows;
    }

    public Map<Integer, String> getDepartmentFullNames() {
        return departmentFullNames;
    }

    public void setDepartmentFullNames(Map<Integer, String> departmentFullNames) {
        this.departmentFullNames = departmentFullNames;
    }
}
