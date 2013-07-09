package com.aplana.sbrf.taxaccounting.web.module.formdata.shared;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.gwtplatform.dispatch.shared.Result;

public class GetRowsDataResult implements Result {


	private PaginatedSearchResult<DataRow<Cell>> dataRows;

	public PaginatedSearchResult<DataRow<Cell>> getDataRows() {
		return dataRows;
	}

	public void setDataRows(PaginatedSearchResult<DataRow<Cell>> dataRows) {
		this.dataRows = dataRows;
	}
}
