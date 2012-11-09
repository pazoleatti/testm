package com.aplana.sbrf.taxaccounting.model.dictionary;

/**
 * Элемент справочника, используемый при заполнении выпадающих списков
 * Каждый элемент представляет собой пару: значение-метка. 
 * @param <ValueType> - тип значения, система поддерживает BigDecimal и String 
 */
public class SimpleDictionaryItem<ValueType> {
	private ValueType value;
	private String name;
	
	public ValueType getValue() {
		return value;
	}
	public void setValue(ValueType value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String label) {
		this.name = label;
	}
}
