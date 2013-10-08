package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared;

import com.gwtplatform.dispatch.shared.Result;

import java.util.Map;

public class GetFieldsNamesResult implements Result {
	Map<FormDataElementName, String> fieldNames;

	public Map<FormDataElementName, String> getFieldNames() {
		return fieldNames;
	}

	public void setFieldNames(Map<FormDataElementName, String> fieldNames) {
		this.fieldNames = fieldNames;
	}
}
