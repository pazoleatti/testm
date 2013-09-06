package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataSearchResultItem;
import com.gwtplatform.dispatch.shared.Result;

import java.util.List;

public class GetDeclarationListResult implements Result {
	private static final long serialVersionUID = 783660987579644789L;

	private List<DeclarationDataSearchResultItem> records;

	//общее количество записей (на всех страницах)
	private long totalCountOfRecords;

	public GetDeclarationListResult() {

	}

	public List<DeclarationDataSearchResultItem> getRecords() {
		return records;
	}

	public void setRecords(List<DeclarationDataSearchResultItem> records) {
		this.records = records;
	}

	public long getTotalCountOfRecords() {
		return totalCountOfRecords;
	}

	public void setTotalCountOfRecords(long totalCountOfRecords) {
		this.totalCountOfRecords = totalCountOfRecords;
	}
}
