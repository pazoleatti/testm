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
			value = new BigDecimal((Integer)value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double)value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long)value);
		}
		if (column instanceof NumericColumn && value != null) {
			int precision = ((NumericColumn) column).getPrecision();
			value = ((BigDecimal)value).setScale(precision, RoundingMode.HALF_UP); 
		}
		
		if (value == null) {
			stringValue = null;
			dateValue = null;
			numericValue = null;
		} else if (value instanceof Date) {
			setDateValue((Date)value);
		} else if (value instanceof BigDecimal) {
			setNumericValue((BigDecimal)value);
		} else if (value instanceof String) {
			setStringValue((String)value);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CellValue cellValue = (CellValue) o;

		if (column != null ? !column.equals(cellValue.column) : cellValue.column != null) return false;

		// TODO: непонятно как сравнивать даты, т.к. этот класс используется как на клиенте, так и на сервере.
		// На клиенте и на сервере используются разные механизмы для работы с датой. В частности на клиенте
		// нельзя использовать java.util.Calendar и java.text.DateFormat.
		// В то же время, нам надо сравнивать только дату, неучитывая часы, минуты и секунды.
		//if (dateValue != null ? !dateValue.equals(cellValue.dateValue) : cellValue.dateValue != null) return false;

		if (numericValue != null ? numericValue.compareTo(cellValue.numericValue)!=0 : cellValue.numericValue != null)
			return false;
		if (stringValue != null ? !stringValue.equals(cellValue.stringValue) : cellValue.stringValue != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stringValue != null ? stringValue.hashCode() : 0;
		//result = 31 * result + (dateValue != null ? dateValue.hashCode() : 0);
		result = 31 * result + (numericValue != null ? numericValue.hashCode() : 0);
		return result;
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
