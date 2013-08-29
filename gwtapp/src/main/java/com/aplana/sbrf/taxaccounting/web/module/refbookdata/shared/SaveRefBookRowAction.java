package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Map;

public class SaveRefBookRowAction extends UnsecuredActionImpl<SaveRefBookRowResult> implements ActionName {

	Long refbookId;
	Long recordId;
	Map<String, RefBookValueSerializable> valueToSave;

	public Long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(Long refbookId) {
		this.refbookId = refbookId;
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

	@Override
	public String getName() {
		return "Сохранить справочник";
	}
}
