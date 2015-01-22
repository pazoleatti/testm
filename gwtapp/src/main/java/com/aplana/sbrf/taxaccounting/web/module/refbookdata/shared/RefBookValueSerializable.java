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
	String dereferenceValue;

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
		if (stringValue != null) {
			stringValue = changeGermanQuotes(stringValue);
			stringValue = changeEnglishSingleQuotes(stringValue);
			stringValue = changeEnglishDoubleQuotes(stringValue);
		}

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

	public String getDereferenceValue() {
		return dereferenceValue;
	}

	public void setDereferenceValue(String dereferenceValue) {
		this.dereferenceValue = dereferenceValue;
	}

	public Object getValue() {
		if (stringValue != null) return stringValue;
		else if (numberValue != null) return numberValue;
		else if (dateValue != null) return dateValue;
		else if (referenceValue != null) return referenceValue;
		else return null;
	}

	/**
	 * Заменить немецкие кавычки („лапки“) на машинописные ""
	 *
	 * @param stringValue „строка с немецкими кавычками“
	 * @return "строка с машинописными кавычками"
	 */
	private String changeGermanQuotes(String stringValue) {
		return stringValue.replaceAll("„", "\"").replaceAll("“", "\"");
	}

	/**
	 * Заменить английские одиночные кавычки (‘английские одиночные’) на машинописные ""
	 *
	 * @param stringValue ‘строка с одиночными английскими кавычками’
	 * @return "строка с машинописными кавычками"
	 */
	private String changeEnglishSingleQuotes(String stringValue) {
		return stringValue.replaceAll("‘", "\"").replaceAll("’", "\"");
	}

	/**
	 * Заменить английские двойные кавычки (“английские двойные”) на машинописные ""
	 *
	 * @param stringValue “строка с двойными английскими кавычками”
	 * @return "строка с машинописными кавычками"
	 */
	private String changeEnglishDoubleQuotes(String stringValue) {
		return stringValue.replaceAll("”", "\"");
	}
}
