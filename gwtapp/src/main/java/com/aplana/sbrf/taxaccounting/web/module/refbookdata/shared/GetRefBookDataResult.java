package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetRefBookDataResult implements Result {
	Map<String, RefBookAttributeSerializable> record;

	public Map<String, RefBookAttributeSerializable> getRecord() {
		return record;
	}

	public void setRecord(Map<String, RefBookAttributeSerializable> record) {
		this.record = record;
	}
}
