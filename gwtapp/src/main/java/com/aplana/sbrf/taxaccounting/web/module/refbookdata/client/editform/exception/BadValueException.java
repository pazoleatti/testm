package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.Map;

public class BadValueException extends Exception {

	private Map<String, String> descriptionMap;

	public BadValueException(Map<String, String> descriptionMap) {
		this.descriptionMap = descriptionMap;
	}

	public BadValueException() {
	}

	public Map<String, String> getDescriptionMap() {
		return descriptionMap;
	}

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
