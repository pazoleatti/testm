package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;

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
public class Cell extends AbstractCell {
    private static final long serialVersionUID = -3684680064726678753L;
	//todo надо задуматься о том, что происходит с датами на двух строчках ниже. Написать тесты
	protected static final Date DATE_1900 = new Date(0,0,1);
	protected static final Date DATE_9999 = new Date(9999 - 1900, 11, 31);

    private String stringValue;
    private Date dateValue;
    private BigDecimal numericValue;
    private boolean editable;

    private String refBookDereference;

    private FormStyle style;
    /** Временный стиль ячейки. Используется только в режиме ручного ввода и не сохраняется в бд */
    private FormStyle clientStyle;

    private List<FormStyle> formStyleList;
    /** Сообщение, формируемое при проверке ячеек */
    private String errorMessage;
    /** Включен ли режим проверки (true - записывать в ячейки, false - бросать исключение) */
    private boolean errorMode;

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

    @Override
    public Object getValue() {
		ColumnType columnType = getColumn().getColumnType();
        // Получаем значение из главной ячейки (SBRFACCTAX-2082)
        if (hasValueOwner()) {
			return null;
        }
		switch (columnType) {
			case AUTO:
			case REFBOOK:
				return numericValue == null ? null : numericValue.longValueExact();
			case STRING: return stringValue;
			case DATE: return dateValue;
			case NUMBER: return numericValue;
			case REFERENCE:
			default: return null;
		}
    }

    @Override
    public Object setValue(Object value, Integer rowNumber) {
		stringValue = null;
		dateValue = null;
		numericValue = null;
		if (value == null) {
			return null;
		}
		// Устанавливаем значение в главную ячейку (SBRFACCTAX-2082)
		if (hasValueOwner()) {
			return null;
		}
		// Формируем заготовки сообщений. Используются при оформлении ошибок
		String columnName = getColumn().getName() == null ? "Без названия" : getColumn().getName();
        String msg = "Графа «" + columnName + "». ";
        String msgValue = "Значение графы «" + columnName + "» ";
        if (rowNumber != null) {
            msg = "Строка " + rowNumber + ": " + msg;
            msgValue = "Строка " + rowNumber + ": " + msgValue;
        }
		// Проверка совместимости типа значения с типом графы
		ColumnType columnType = getColumn().getColumnType();
        if (!(value instanceof Number && (ColumnType.NUMBER.equals(columnType) || ColumnType.REFBOOK.equals(columnType) ||
				ColumnType.REFERENCE.equals(columnType) || ColumnType.AUTO.equals(columnType))
                || value instanceof String && ColumnType.STRING.equals(columnType)
                || value instanceof Date && ColumnType.DATE.equals(columnType))) {
            return showError(msg + "Несовместимые типы колонки и значения");
        }
		switch (columnType) {
			case AUTO:
			case NUMBER:
			case REFBOOK:{
				// Допустимы для установки значений только типы Integer, Long, Double и BigDecimal
				if (value instanceof Integer) {
					value = new BigDecimal((Integer) value);
				} else if (value instanceof Double) {
					value = new BigDecimal((Double) value);
				} else if (value instanceof Long) {
					value = new BigDecimal((Long) value);
				} else if (!(value instanceof BigDecimal)) {
					return showError(msg + "Несовместимые типы графы и значения. Тип значения: \"" + value.getClass().getName() +
                            "\", типа графы: \"" + columnType.getTitle() + "\". " +
                            "Значение должно иметь тип Integer, Long или BigDecimal. Для автонумеруемых и справочных граф еще и без дробной части");
				}

				if (ColumnType.NUMBER.equals(columnType)) {
					int precision = ((NumericColumn) getColumn()).getPrecision();
					value = ((BigDecimal) value).setScale(precision, RoundingMode.HALF_UP);
					String str = ((BigDecimal) value).toPlainString();
					if (!getColumn().getValidationStrategy().matches(str)) {
                        NumericColumn numericColumn = (NumericColumn) getColumn();
						return showError(msgValue + "превышает допустимую разрядность. Должно быть не более " +
                                (numericColumn.getMaxLength()-numericColumn.getPrecision()) + " знакомест и не более " + numericColumn.getPrecision() +
                                " знаков после запятой. Устанавливаемое значение: " + str);
					}
				} else { // ColumnType.AUTO ColumnType.REFBOOK
					value = ((BigDecimal) value).setScale(0, RoundingMode.HALF_UP);
					if (!getColumn().getValidationStrategy().matches(((BigDecimal) value).toPlainString())) {
						return showError(msgValue + "превышает допустимую разрядность (19 знаков)!");
					}
				}
				numericValue = (BigDecimal) value;
				return getValue();
			}
			case STRING: {
				if (!getColumn().getValidationStrategy().matches((String) value)) {
					return showError(msg + "содержит значение '" +
                            value + "' длиннее " + ((StringColumn) getColumn()).getMaxLength());
				}
				stringValue = (String) value;
				return getValue();
			}
			case DATE: {
                Date date = (Date) value;
                if (date.before(DATE_1900)) { // Сделано из-за ограничений Excel при работе с датами SBRFACCTAX-9982
                    return showError(msg + "Не может быть указана более ранняя дата, чем 01.01.1900!");
                }
                if (date.after(DATE_9999)) {
                    return showError(msg + "Не может быть указана более поздняя дата, чем 31.12.9999!");
                }
				dateValue = date;
				return getValue();
			}
			case REFERENCE: {
				return showError(msg + "Нельзя устанавливать значения в зависимую графу!");
			}
			default: {
				return showError("Values of type " + value.getClass().getName() + " are not supported");
			}
		}
    }

    /** Записывает сообщение об ошибке в ячейку или выбрасывает исключение */
    private Object showError(String msg) {
        if (errorMode) {
            errorMessage = msg;
        } else {
            throw new IllegalArgumentException(msg);
        }
        return null;
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
     * Не использовать в проверках в скриптах, т.к. есть режим ручного ввода!
     *
     * @return что ячейка допускает ввод значения пользователем
     */
    public boolean isEditable() {
        return editable;
    }


    /**
     * Задаёт признак того, что ячейка допускает ввод значения пользователем
     *
     * @param editable true - пользователь может вводить значения, false - пользователь не может вводить значения
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }


    /**
     * Получить {@link FormStyle стиль}, связанный с ячейкой.
     * Если значение стиля равно null, то нужно использовать стиль по-умолчанию.
     *
     * @return стиль, связанный с ячейкой
     */
    public FormStyle getStyle() {
        if (!ModelUtils.containsLink(formStyleList, style)) {
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
     * @param styleAlias {@link FormStyle#getAlias() алиас стиля}, связанного с
     *                   ячейкой.
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

    /**
     * Возвращает разименованное значение справочника
     * !!! Используется только в GUI
     * SBRFACCTAX-3219
     *
     * @return
     */
    public String getRefBookDereference() {
        return refBookDereference;
    }

    /**
     * Возвращает разименованное значение справочника
     * !!! Используется только в GUI
     * SBRFACCTAX-3219
     *
     * @param refBookDereference
     */
    public void setRefBookDereference(String refBookDereference) {
        this.refBookDereference = refBookDereference;
    }

    /**
     * Устанавливает цвет фона и шрифта. Использовать только в режиме ручного ввода для жесткого задания цвета ячеек!
     * @param alias алиас для нового стиля
     * @param fontColor цвет шрифта
     * @param backColor цвет фона
     */
    public void setClientStyle(String alias, Color fontColor, Color backColor) {
        if (formStyleList != null && !formStyleList.isEmpty()) {
            clientStyle = new FormStyle();
            clientStyle.setAlias(alias);
            clientStyle.setBackColor(backColor);
            clientStyle.setFontColor(fontColor);
            formStyleList.add(clientStyle);
        }
    }

    public FormStyle getClientStyle() {
        return clientStyle;
    }

    public String getMessage() {
        return errorMessage;
    }

    public void setMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setCheckMode(boolean errorMode) {
        this.errorMode = errorMode;
    }

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Cell{");
		sb.append("value=").append(getValue());
		sb.append("; dereference=").append(getRefBookDereference());
		sb.append("; colspan=").append(getColSpan());
		sb.append("; rowspan=").append(getRowSpan());
		sb.append("; style=").append(getStyle());
		sb.append("; editable=").append(isEditable());
		sb.append('}');

		return sb.toString();
	}
}
