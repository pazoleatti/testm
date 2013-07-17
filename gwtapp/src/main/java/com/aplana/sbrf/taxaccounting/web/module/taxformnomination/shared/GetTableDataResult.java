package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTableDataResult implements Result {

    private List<FormTypeKind> tableData;


    public List<FormTypeKind> getTableData() {
        return tableData;
    }

    public void setTableData(List<FormTypeKind> tableData) {
        this.tableData = tableData;
    }
}
