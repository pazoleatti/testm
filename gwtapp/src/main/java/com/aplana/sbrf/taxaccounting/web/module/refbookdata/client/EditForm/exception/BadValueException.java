package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception;

public class BadValueException extends Exception {
	String fieldName;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}
