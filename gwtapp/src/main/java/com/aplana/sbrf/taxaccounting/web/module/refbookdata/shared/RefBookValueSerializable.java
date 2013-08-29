package com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;

import java.io.Serializable;
import java.util.Date;

public class RefBookValueSerializable implements Serializable {

	private static final long serialVersionUID = -5912108710642757399L;
	private RefBookAttributeType attributeType;

	String stringValue;
	Number numberValue;
	Date dateValue;
	Long referenceValue;

	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(RefBookAttributeType attributeType) {
		this.attributeType = attributeType;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public Number getNumberValue() {
		return numberValue;
	}

	public void setNumberValue(Number numberValue) {
		this.numberValue = numberValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public Long getReferenceValue() {
		return referenceValue;
	}

	public void setReferenceValue(Long referenceValue) {
		this.referenceValue = referenceValue;
	}

	public Object getValue() {
		if (stringValue != null) return stringValue;
		else if (numberValue != null) return numberValue;
		else if (dateValue != null) return dateValue;
		else if (referenceValue != null) return referenceValue;
		else return null;
	}
}
