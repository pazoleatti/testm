package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetRefBookTableDataResult implements Result {

	List<RefBookAttribute> tableHeaders;
	List<RefBookDataRow> dataRows;

	public List<RefBookAttribute> getTableHeaders() {
		return tableHeaders;
	}

	public void setTableHeaders(List<RefBookAttribute> tableHeaders) {
		this.tableHeaders = tableHeaders;
	}

	public List<RefBookDataRow> getDataRows() {
		return dataRows;
	}

	public void setDataRows(List<RefBookDataRow> dataRows) {
		this.dataRows = dataRows;
	}
}
