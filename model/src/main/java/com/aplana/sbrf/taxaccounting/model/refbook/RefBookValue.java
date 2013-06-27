package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;

/**
 * Элемент справочника. Представляет собой ячейку таблицы.
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 23.05.13 17:43
 */
public class RefBookValue implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Тип хранящегося значения */
	private RefBookAttributeType attributeType;

	/** Хранит значение вещественного типа */
	private Object value;

	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(RefBookAttributeType attributeType) {
		this.attributeType = attributeType;
	}

	public Date getDateValue() {
		if (attributeType == RefBookAttributeType.DATE) {
			return (Date) value;
		}
		return null;
	}

	public Number getNumberValue() {
		if (attributeType == RefBookAttributeType.NUMBER) {
			return (Number) value;
		}
		return null;
	}

	public String getStringValue() {
		if (attributeType == RefBookAttributeType.STRING) {
			return (String) value;
		}
		return null;
	}

	private Object getValue() {
		return value;
	}

	private void setValue(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RefBookValue{");
		sb.append("attributeType=").append(attributeType);
		sb.append(", value=").append(value);
		sb.append('}');
		return sb.toString();
	}
}