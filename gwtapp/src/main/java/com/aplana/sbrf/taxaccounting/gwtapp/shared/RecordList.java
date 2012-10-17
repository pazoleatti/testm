package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import java.util.List;

import com.gwtplatform.dispatch.shared.Result;

public class RecordList<T> implements Result  {
	private List<T> records;

	public RecordList(List<T> records) {
		this.records = records;
	}
	
	public List<T> getRecords() {
		return records;
	}

	public void setRecords(List<T> records) {
		this.records = records;
	}	
}