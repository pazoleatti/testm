package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class CellValue implements Serializable {
	private static final long serialVersionUID = 1L;
	private String stringValue;
	private Date dateValue;
	private BigDecimal numericValue;
	private Column column;
	
	// Значение диапазона (пока используется для объединения ячеек)
	private Integer colSpan = 1;
	private Integer rowSpan = 1;

	public Object getValue() {
		if (stringValue != null) {
			return stringValue;
		} else if (dateValue != null) {
			return dateValue;
		} else if (numericValue != null) {
			return numericValue;
		}
		return null;
	}

	public Object setValue(Object value) {
		if (value instanceof Integer) {
			value = new BigDecimal((Integer) value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double) value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long) value);
		}
		if (column instanceof NumericColumn && value != null) {
			int precision = ((NumericColumn) column).getPrecision();
			value = ((BigDecimal) value).setScale(precision, RoundingMode.HALF_UP);
		}

		if (value == null) {
			stringValue = null;
			dateValue = null;
			numericValue = null;
		} else if (value instanceof Date) {
			setDateValue((Date) value);
		} else if (value instanceof BigDecimal) {
			setNumericValue((BigDecimal) value);
		} else if (value instanceof String) {
			setStringValue((String) value);
		} else {
			throw new IllegalArgumentException("Values of type " + value.getClass().getName() + " are not supported");
		}
		return getValue();
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		dateValue = null;
		numericValue = null;
		this.stringValue = stringValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		stringValue = null;
		numericValue = null;
		this.dateValue = dateValue;
	}

	public BigDecimal getNumericValue() {
		return numericValue;
	}

	public void setNumericValue(BigDecimal numericValue) {
		stringValue = null;
		dateValue = null;
		this.numericValue = numericValue;
	}

	public Column getColumn() {
		return column;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	/**
	 * Возвращает количество столбцов, на которые должна "растягиваться" данная ячейка (аналогично атрибуту colspan html-тега TD)
	 * @return значение атрибута colSpan 
	 */
	public Integer getColSpan() {
		return colSpan;
	}

	/**
	 * Задаёт количество столбцов, на которые должна "растягиваться" данная ячейка (аналогично атрибуту colspan html-тега TD)
	 * Если значение null, то объединение ячеек не требуется 
	 * @param colSpan значение атрибута colSpan
	 * @throws IllegalArgumentException если задаётся значение меньше 2
	 */	
	public void setColSpan(Integer colSpan) {
		if (colSpan != null && colSpan <= 1) {
			throw new IllegalArgumentException("colSpan value must be greater than 1");
		}		
		this.colSpan = colSpan;
	}

	/**
	 * Возвращает количество строк, на которые должна "растягиваться" данная ячейка (аналогично атрибуту rowspan html-тега TD)
	 * @return значение атрибута rowSpan 
	 */
	public Integer getRowSpan() {
		return rowSpan;
	}

	/**
	 * Задаёт количество столбцов, на которые должна "растягиваться" данная ячейка (аналогично атрибуту rowspan html-тега TD)
	 * Если значение null, то объединение ячеек не требуется 
	 * @param rowSpan значение атрибута rowSpan
	 * @throws IllegalArgumentException если задаётся значение меньше 2 
	 */	
	public void setRowSpan(Integer rowSpan) {
		if (rowSpan != null && rowSpan <= 1) {
			throw new IllegalArgumentException("rowSpan value must be greater than 1");
		}
		this.rowSpan = rowSpan;
	}

	@Override
	public String toString() {
		return "CellValue{" +
				"stringValue='" + stringValue + '\'' +
				", dateValue=" + dateValue +
				", numericValue=" + numericValue +
				'}';
	}
}
