package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared;


import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetTableDataResult implements Result {
	private static final long serialVersionUID = -6048041608774139006L;
	
	private List<FormTypeKind> tableData;

    public List<FormTypeKind> getTableData() {
        return tableData;
    }

    public void setTableData(List<FormTypeKind> tableData) {
        this.tableData = tableData;
    }
    
}
