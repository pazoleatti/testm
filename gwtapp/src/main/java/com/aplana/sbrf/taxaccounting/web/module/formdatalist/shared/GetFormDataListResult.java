package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

public class GetFormDataListResult implements Result {
	private List<FormData> records;	
	
	public GetFormDataListResult() {
		
	}
	
	public GetFormDataListResult(List<FormData> records) {
		this.records = records;
	}
	
	public List<FormData> getRecords() {
		return records;
	}

	public void setRecords(List<FormData> records) {
		this.records = records;
	}	
}