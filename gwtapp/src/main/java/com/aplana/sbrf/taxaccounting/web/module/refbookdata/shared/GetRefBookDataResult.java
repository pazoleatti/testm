package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetRefBookDataResult implements Result {
	Map<String, RefBookAttribute> record;

	public Map<String, RefBookAttribute> getRecord() {
		return record;
	}

	public void setRecord(Map<String, RefBookAttribute> record) {
		this.record = record;
	}
}
