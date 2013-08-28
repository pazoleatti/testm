package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Comp-1
 * Date: 28.08.13
 * Time: 14:52
 * To change this template use File | Settings | File Templates.
 */
public class GetRefBookRecordResult implements Result {
	Map<String, RefBookValueSerializable> record;

	public Map<String, RefBookValueSerializable> getRecord() {
		return record;
	}

	public void setRecord(Map<String, RefBookValueSerializable> record) {
		this.record = record;
	}
}
