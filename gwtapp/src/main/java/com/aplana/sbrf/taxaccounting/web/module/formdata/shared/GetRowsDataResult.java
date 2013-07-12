package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.gwtplatform.dispatch.shared.Result;

public class GetRowsDataResult implements Result {
	private static final long serialVersionUID = -6510327178774833524L;
	
	private PagingResult<DataRow<Cell>> dataRows;

	public PagingResult<DataRow<Cell>> getDataRows() {
		return dataRows;
	}

	public void setDataRows(PagingResult<DataRow<Cell>> dataRows) {
		this.dataRows = dataRows;
	}
}
