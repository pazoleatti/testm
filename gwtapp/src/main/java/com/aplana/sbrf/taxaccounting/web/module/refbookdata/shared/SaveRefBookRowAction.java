package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Map;

public class SaveRefBookRowAction extends UnsecuredActionImpl<SaveRefBookRowResult> implements ActionName {

	Long refbookId;
	Map<String, RefBookAttribute> valueToSave;

	public Long getRefbookId() {
		return refbookId;
	}

	public void setRefbookId(Long refbookId) {
		this.refbookId = refbookId;
	}

	public Map<String, RefBookAttribute> getValueToSave() {
		return valueToSave;
	}

	public void setValueToSave(Map<String, RefBookAttribute> valueToSave) {
		this.valueToSave = valueToSave;
	}

	@Override
	public String getName() {
		return "";
	}
}
