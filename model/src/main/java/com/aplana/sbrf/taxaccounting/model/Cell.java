package com.aplana.sbrf.taxaccounting.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;

/**
 * Класс, содержащий информацию о ячейке таблицы налоговой формы: значение,
 * стиль оформления, параметры объединения ячеек и т.п.
 * 
 * @author dsultanbekov
 */
public class Cell extends AbstractCell {
	private static final long serialVersionUID = -3684680064726678753L;
	
	private String stringValue;
	private Date dateValue;
	private BigDecimal numericValue;
	private boolean editable;

	private FormStyle style;
	
	private List<FormStyle> formStyleList;
	
	/**
	 * Конструктор только для сериализации
	 */
	public Cell() {
		super();
	}

	public Cell(Column column, List<FormStyle> formStyleList) {
		super(column);
		this.formStyleList = formStyleList;
	}

	public Object getValue() {
		// Получаем значение из главной ячейки (SBRFACCTAX-2082)
		if (hasValueOwner()){
			return getValueOwner().getValue();
		}
		
		if (stringValue != null) {
			return stringValue;
		} else if (dateValue != null) {
			return dateValue;
		} else if (numericValue != null) {
			if (getColumn() instanceof RefBookColumn){
				// Возвращаем Long если это ссылка на значение справочника
				return numericValue.longValueExact();
			} else {
				return numericValue;
			}
		} 
		return null;
	}

	public Object setValue(Object value) {
		// Устанавливаем значение в главную ячейку (SBRFACCTAX-2082)
		if (hasValueOwner()){
			getValueOwner().setValue(value);
			return getValueOwner().getValue();
		}
		
		// Проверяем на предмет столбца - справочник
		if (getColumn() instanceof RefBookColumn){
			if (value instanceof Long){
				numericValue = BigDecimal.valueOf((Long)value);
			} else {
				throw new IllegalArgumentException("Несовместимые типы колонки и значения. Ссылка на справочник должна быть типа Long");
			}
		}
		
		
		if ( (value != null) && !(value instanceof Number && getColumn() instanceof NumericColumn
				|| value instanceof String && getColumn() instanceof StringColumn
				|| value instanceof Date && getColumn() instanceof DateColumn)) {
			throw new IllegalArgumentException("Несовместимые типы колонки и значения");
		}
		if (value instanceof Integer) {
			value = new BigDecimal((Integer) value);
		} else if (value instanceof Double) {
			value = new BigDecimal((Double) value);
		} else if (value instanceof Long) {
			value = new BigDecimal((Long) value);
		}
		if (getColumn() instanceof NumericColumn && value != null) {
			int precision = ((NumericColumn) getColumn()).getPrecision();
			value = ((BigDecimal) value).setScale(precision,
					RoundingMode.HALF_UP);
			if (!getColumn().getValidationStrategy().matches(((BigDecimal) value).toPlainString())) {
				throw new IllegalArgumentException("Число " + ((BigDecimal) value).toPlainString() +
						" не соответствует формату " +
						(((NumericColumn) getColumn()).getMaxLength() - ((NumericColumn) getColumn()).getPrecision()) + "." +
						((NumericColumn) getColumn()).getPrecision());
			}
		} else if (getColumn() instanceof StringColumn) {
			if (!getColumn().getValidationStrategy().matches((String) value)) {
				throw new IllegalArgumentException(((String) value) + " длинее " + ((StringColumn)getColumn()).getMaxLength());
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
	
	
	public void setStyleId(Integer styleId) {
		if (styleId == null) {
			style = null;
			return;
		}
		for (FormStyle formStyle : formStyleList) {
			if (formStyle.getAlias() != null
					&& formStyle.getId().equals(styleId)) {
				style = formStyle;
				return;
			}
		}
		throw new IllegalArgumentException("Стиля с id '" + styleId
				+ "' не существует в шаблоне");
	}
	

	public String getStyleAlias() {
		return style != null ? style.getAlias() : null;
	}


}
