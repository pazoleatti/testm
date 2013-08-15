package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetRefBookTableDataResult implements Result {

	List<RefBookAttribute> tableHeaders;
	List<RefBookDataRow> dataRows;
	int totalCount;

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

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}
