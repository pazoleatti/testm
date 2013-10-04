package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm.exception;

public class BadValueException extends Exception {
	String fieldName;
	String description;

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
