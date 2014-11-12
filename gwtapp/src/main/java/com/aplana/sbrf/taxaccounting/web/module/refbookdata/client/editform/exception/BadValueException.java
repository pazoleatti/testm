package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

public class BadValueException extends Exception {

	String fieldName;
	String description;

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Атрибут \"" + fieldName + "\": " + description;
	}
}
