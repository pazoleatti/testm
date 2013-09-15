package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class AddRefBookRowAction extends UnsecuredActionImpl<AddRefBookRowResult> implements ActionName {

	List<Map<String, RefBookValueSerializable>> records;
	long refBookId;
	Date relevanceDate;

	public List<Map<String, RefBookValueSerializable>> getRecords() {
		return records;
	}

	public void setRecords(List<Map<String, RefBookValueSerializable>> records) {
		this.records = records;
	}

	public long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(long refBookId) {
		this.refBookId = refBookId;
	}

	public Date getRelevanceDate() {
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

	@Override
	public String getName() {
		return "Добавить запись в справочник";
	}
}
