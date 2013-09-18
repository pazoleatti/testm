package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;
import java.util.Map;

public class SaveRefBookRowAction extends UnsecuredActionImpl<SaveRefBookRowResult> implements ActionName {

	Long refBookId;
	Long recordId;
	Map<String, RefBookValueSerializable> valueToSave;
	Date relevanceDate;

	public Long getRefBookId() {
		return refBookId;
	}

	public void setRefBookId(Long refBookId) {
		this.refBookId = refBookId;
	}

	public Long getRecordId() {
		return recordId;
	}

	public void setRecordId(Long recordId) {
		this.recordId = recordId;
	}

	public Map<String, RefBookValueSerializable> getValueToSave() {
		return valueToSave;
	}

	public void setValueToSave(Map<String, RefBookValueSerializable> valueToSave) {
		this.valueToSave = valueToSave;
	}

	public Date getRelevanceDate() {
		return relevanceDate;
	}

	public void setRelevanceDate(Date relevanceDate) {
		this.relevanceDate = relevanceDate;
	}

	@Override
	public String getName() {
		return "Сохранить справочник";
	}
}
