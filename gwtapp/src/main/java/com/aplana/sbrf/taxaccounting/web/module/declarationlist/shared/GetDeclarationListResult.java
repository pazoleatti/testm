package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationListResult implements Result {

	private List<DeclarationSearchResultItem> records;

	//общее количество записей (на всех страницах)
	private long totalCountOfRecords;

	public GetDeclarationListResult() {

	}

	public List<DeclarationSearchResultItem> getRecords() {
		return records;
	}

	public void setRecords(List<DeclarationSearchResultItem> records) {
		this.records = records;
	}

	public long getTotalCountOfRecords() {
		return totalCountOfRecords;
	}

	public void setTotalCountOfRecords(long totalCountOfRecords) {
		this.totalCountOfRecords = totalCountOfRecords;
	}
}
