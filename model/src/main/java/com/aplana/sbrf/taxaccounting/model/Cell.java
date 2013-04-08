package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * Класс, содержащий информацию о ячейке таблицы налоговой формы: значение,
 * стиль оформления, параметры объединения ячеек и т.п.
 * 
 * @author dsultanbekov
 */
public class Cell implements Serializable {
	private static final long serialVersionUID = 1L;
	private String stringValue;
	private Date dateValue;
	private BigDecimal numericValue;
	private Column column;
	private boolean editable;

	private List<FormStyle> formStyleList;
	private FormStyle style;

	// Значение диапазона (пока используется для объединения ячеек)
	private int colSpan = 1;
	private int rowSpan = 1;

	/**
	 * Конструктор только для сериализации
	 */
	public Cell() {

	}

	public Cell(Column column, List<FormStyle> formStyleList) {
		this.column = column;
		this.formStyleList = formStyleList;
	}

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
		if ( (value != null) && !(value instanceof Number && column instanceof NumericColumn
				|| value instanceof String && column instanceof StringColumn
				|| value instanceof Date && column instanceof DateColumn)) {
			throw new IllegalArgumentException("Несовместимые типы колонки и значения");
		}
		if (value instanceof Integer) {
			value = new BigDecimal((Integer) value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double) value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long) value);
		}
		if (column instanceof NumericColumn && value != null) {
			int precision = ((NumericColumn) column).getPrecision();
			value = ((BigDecimal) value).setScale(precision,
					RoundingMode.HALF_UP);
			if (!column.getValidationStrategy().matches(((BigDecimal) value).toPlainString())) {
				throw new IllegalArgumentException("Число " + ((BigDecimal) value).toPlainString() +
						" не соответствует формату " +
						(((NumericColumn) column).getMaxLength() - ((NumericColumn) column).getPrecision()) + "." +
						((NumericColumn) column).getPrecision());
			}
		} else if (column instanceof StringColumn) {
			if (!column.getValidationStrategy().matches((String) value)) {
				throw new IllegalArgumentException(((String) value) + " длинее " + ((StringColumn)column).getMaxLength());
			}
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
			throw new IllegalArgumentException("Values of type "
					+ value.getClass().getName() + " are not supported");
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
	 * Возвращает количество столбцов, на которые должна "растягиваться" данная
	 * ячейка (аналогично атрибуту colspan html-тега TD)
	 * 
	 * @return значение атрибута colSpan
	 */
	public int getColSpan() {
		return colSpan;
	}

	/**
	 * Задаёт количество столбцов, на которые должна "растягиваться" данная
	 * ячейка (аналогично атрибуту colspan html-тега TD) Если значение 1, то
	 * объединение ячеек не требуется
	 * 
	 * @param colSpan
	 *            значение атрибута colSpan
	 * @throws IllegalArgumentException
	 *             если задаётся значение меньше 1
	 */
	public void setColSpan(int colSpan) {
		if (colSpan < 1) {
			throw new IllegalArgumentException(
					"colSpan value can not be less than 1");
		}
		this.colSpan = colSpan;
	}

	/**
	 * Возвращает количество строк, на которые должна "растягиваться" данная
	 * ячейка (аналогично атрибуту rowspan html-тега TD)
	 * 
	 * @return значение атрибута rowSpan
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * Задаёт количество строк, на которые должна "растягиваться" данная ячейка
	 * (аналогично атрибуту rowspan html-тега TD) Если значение 1, то
	 * объединение ячеек не требуется
	 * 
	 * @param rowSpan
	 *            значение атрибута rowSpan
	 * @throws IllegalArgumentException
	 *             если задаётся значение меньше 1
	 */
	public void setRowSpan(int rowSpan) {
		if (rowSpan < 1) {
			throw new IllegalArgumentException(
					"rowSpan value can not be less than 1");
		}
		this.rowSpan = rowSpan;
	}
	

	/**
	 * Признак того, что ячейка допускает ввод значения пользователем
	 * @return что ячейка допускает ввод значения пользователем
	 */
	public boolean isEditable() {
		return editable;
	}
	

	/**
	 * Задаёт признак того, что ячейка допускает ввод значения пользователем
	 * @param editable true - пользователь может вводить значения, false - пользователь не может вводить значения
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	

	/**
	 * Получить {@link FormStyle стиль}, связанный с ячейкой.
	 * Если значение стиля равно null, то нужно использовать стиль по-умолчанию.
	 * @return стиль, связанный с ячейкой
	 */
	public FormStyle getStyle() {
		if (!ModelUtils.containsLink(formStyleList, style)){
			// Обнуляем отсутствующий стиль
			style = null;
		}
		return style;
	}

	/**
	 * Задать {@link FormStyle#getAlias() алиас стиля}, связанного с ячейкой.
	 * Стиль с таким алиасом должен быть определён в
	 * {@link FormTemplate#getStyles() коллекции стилей}, связанных с шаблоном
	 * налоговой формы
	 * 
	 * @param styleAlias
	 *            {@link FormStyle#getAlias() алиас стиля}, связанного с
	 *            ячейкой.
	 */
	public void setStyleAlias(String styleAlias) {
		if (styleAlias == null) {
			style = null;
			return;
		}
		for (FormStyle formStyle : formStyleList) {
			if (formStyle.getAlias() != null
					&& formStyle.getAlias().equals(styleAlias)) {
				style = formStyle;
				return;
			}
		}
		throw new IllegalArgumentException("Стиля с алиасом '" + styleAlias
				+ "' не существует в шаблоне");
	}

	public String getStyleAlias() {
		return style != null ? style.getAlias() : null;
	}

	@Override
	public String toString() {
		return "CellValue [stringValue=" + stringValue + ", dateValue="
				+ dateValue + ", numericValue=" + numericValue + ", column="
				+ column + ", colSpan=" + colSpan + ", rowSpan=" + rowSpan
				+ "]";
	}
}
