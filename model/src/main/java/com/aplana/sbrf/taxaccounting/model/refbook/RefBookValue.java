package com.aplana.sbrf.taxaccounting.model.refbook;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

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

	/**
	 * Возвращает значение ссылки в виде кода записи справочника
	 * @return
	 */
	public Long getReferenceValue() {
		if (value != null && attributeType == RefBookAttributeType.REFERENCE && value instanceof Long) {
			return (Long) value;
		}
		return null;
	}

	/**
	 * Возвращает значение ссылки в виде объекта
	 * @return
	 */
	public Map<String, RefBookValue> getReferenceObject() {
		if (value != null && attributeType == RefBookAttributeType.REFERENCE && value instanceof Map) {
			return (Map<String, RefBookValue>) value;
		}
		return null;
	}

	/**
	 * Установка значения. При этом происходит проверка на тип значения.
	 * @param value устанавливаемое значение. Для все типов, кроме ссылки значение является примитивом. Для
	 *              атрибутов-ссылок в качестве значения можно передавать как код строки (примитив), на которую
	 *              ссылаемся, так и непосредственно сам объект-строка.
	 */
	public void setValue(Object value) {
		if (value == null ||
				(attributeType == RefBookAttributeType.NUMBER && value instanceof Number) ||
				(attributeType == RefBookAttributeType.STRING && value instanceof String) ||
				(attributeType == RefBookAttributeType.DATE && value instanceof Date) ||
				(attributeType == RefBookAttributeType.REFERENCE && (value instanceof Long || value instanceof Map))) {
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

        return attributeType == that.attributeType
                && (value == null || that.value != null)
                && (value != null || that.value == null)
                && (value == null || that.value == null || value.equals(that.value));
    }

	@Override
	public int hashCode() {
		return 31 * attributeType.hashCode() + (value == null ? 0 : value.hashCode());
	}

    @Override
	public String toString() {
        return value == null ? "" : value.toString();
	}
}