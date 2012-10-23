package com.aplana.sbrf.taxaccounting.gwtapp.shared;

import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.gwtapp.client.util.DataRowUtil;
import com.aplana.sbrf.taxaccounting.model.Form;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.gwtplatform.dispatch.shared.Result;

public class GetFormDataResult implements Result {
	private Form form;
	private List<Map<String, Object>> rowsData;
	public FormData getFormData() {
		FormData formData = new FormData(form);
		for(Map<String, Object> rowDataMap: rowsData) {
			DataRowUtil.addRowToFormData(formData, rowDataMap);
		}
		return formData;
	}
	
	public void setForm(Form form) {
		this.form = form;
	}
	
	public void setRowsData(List<Map<String, Object>> rowsData) {
		this.rowsData = rowsData;
	}
}