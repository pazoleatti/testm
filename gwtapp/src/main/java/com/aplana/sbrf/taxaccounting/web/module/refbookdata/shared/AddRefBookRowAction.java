package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;
import java.util.Map;

public class AddRefBookRowAction extends UnsecuredActionImpl<AddRefBookRowResult> implements ActionName {

	List<Map<String, RefBookAttributeSerializable>> records;
	long refbookId;

	public List<Map<String, RefBookAttributeSerializable>> getRecords() {
		return records;
	}

	public void setRecords(List<Map<String, RefBookAttributeSerializable>> records) {
		this.records = records;
	}

	public long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(long refbookId) {
		this.refbookId = refbookId;
	}

	@Override
	public String getName() {
		return "Добавить запись в справочник";
	}
}
