package com.aplana.sbrf.taxaccounting.web.module.refbooklist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

/**
 * @author Stanislav Yasinskiy
 */
public class GetTableDataResult implements Result {

    private List<TableModel> tableData;


    public List<TableModel> getTableData() {
        return tableData;
    }

    public void setTableData(List<TableModel> tableData) {
        this.tableData = tableData;
    }
}
