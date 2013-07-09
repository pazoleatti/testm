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

	public RefBookValue(RefBookAttributeType attributeType, Object value) {
		this.attributeType = attributeType;
		setValue(value);
	}

	public RefBookAttributeType getAttributeType() {
		return attributeType;
	}

	public String getStringValue() {
		if (value != null && attributeType == RefBookAttributeType.STRING) {
			return (String) value;
		}
		return null;
	}

	public Number getNumberValue() {
		if (value != null && attributeType == RefBookAttributeType.NUMBER) {
			return (Number) value;
		}
		return null;
	}

	public Date getDateValue() {
		if (value != null && attributeType == RefBookAttributeType.DATE) {
			return (Date) value;
		}
		return null;
	}

	public Long getReferenceValue() {
		if (value != null && attributeType == RefBookAttributeType.REFERENCE) {
			return (Long) value;
		}
		return null;
	}

	/**
	 * Установка значения. При этом происходит проверка на тип значения.
	 * @param value устанавливаемое значение
	 */
	private void setValue(Object value) {
		if (value == null ||
				(attributeType == RefBookAttributeType.NUMBER && value instanceof Number) ||
				(attributeType == RefBookAttributeType.STRING && value instanceof String) ||
				(attributeType == RefBookAttributeType.DATE && value instanceof Date) ||
				(attributeType == RefBookAttributeType.REFERENCE && value instanceof Long)) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(String.format("Illegal argument type. Must be \"%s\" instead of \"%s\"",  attributeType.name(), value.getClass().getName()));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RefBookValue that = (RefBookValue) o;

		if (attributeType != that.attributeType) return false;
		if (!value.equals(that.value)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = attributeType.hashCode();
		result = 31 * result + value.hashCode();
		return result;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(attributeType.name().charAt(0));
		sb.append(":");
		sb.append(value);
		return sb.toString();
	}
}