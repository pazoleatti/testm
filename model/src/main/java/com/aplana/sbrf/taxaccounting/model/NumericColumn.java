package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Реализация {@link Column}, предназначенная для хранения числовых данных.
 * Числовые данные в налоговых формах всегда хранятся в типе данных {@link BigDecimal}.
 * <p/>
 * У столбцов данного типа есть дополнительная опция настройки - {@link #getPrecision() precision}, которая задаёт
 * количество знаков после запятой, которое допустимо в данном столбце
 *
 * @author dsultanbekov
 */
public class NumericColumn extends Column implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Максимально допустимое значение точности для числового столбца
	 * (ограничение налагается возможностями БД и деталями описания таблицы NUMERIC_VALUE)
	 */
	public static final int MAX_PRECISION = 19;

	/**
	 * Максимально допустимое колличество значений для числового столбца
	 * (ограничение налагается возможностями БД и деталями описания таблицы NUMERIC_VALUE)
	 */
	public static final int MAX_LENGTH = 38;

	private int precision = 0;

	private int maxLength = MAX_LENGTH;

	private static Formatter formatter;

	public NumericColumn() {
		setColumnType(ColumnType.NUMBER);
	}

	/**
	 * Задает точность столбца, т.е. колличество знаков справа от запятой. Аналогично положительным {@link BigDecimal}
	 *
	 * @return возвращает точность
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * Задаёт точность числового столбца
	 * Точность столбца задаёт количество знаков после запятой, которые допустимы в данном столбце, для целых чисел используентся значение 0
	 *
	 * @param precision желаемое значение точности
	 */
	public void setPrecision(int precision) {
		if (precision < 0 || precision > MAX_PRECISION) {
			throw new IllegalArgumentException("Value " + precision + " is not supported by 'precision' field");
		}
		this.precision = precision;
	}

	/**
	 * Возвращает значение точности числового столбца
	 * Точность столбца задаёт общее количество знаков, которые допустимы в данном столбце.
	 *
	 * @return значение точности
	 */
	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
        if (maxLength<0||maxLength>MAX_LENGTH){
            throw new IllegalArgumentException("Value " + precision + " is not supported by 'precision' field");
        }
		this.maxLength = maxLength;
	}

	@Override
	public Formatter getFormatter() {
		return formatter != null ? formatter :
			new Formatter() {
				@Override
				public String format(String valueToFormat) {
					valueToFormat = valueToFormat.replace(" ", "");
					MathContext mathContext = new MathContext(maxLength);
					BigDecimal val = new BigDecimal(valueToFormat, mathContext);
					val = val.setScale(precision, RoundingMode.HALF_UP);

					boolean hasSign = (val.signum() == -1);
					if (hasSign) {
						val = val.abs();
					}
                    int intLength = val.precision() - val.scale();
                    StringBuilder stringBuilder = new StringBuilder(val.toPlainString().substring(0, intLength));

                    for (int i = 3; i < intLength; i += 3) {
                        if (i < intLength) {
                            stringBuilder.insert(intLength - i, " ");
                        }
                    }
                    stringBuilder.append(val.toPlainString().substring(intLength, val.toPlainString().length()));
                    if (hasSign) {
                        stringBuilder.insert(0, "-");
                    }
                    return stringBuilder.toString();
                }
			};
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return new ValidationStrategy() {
			@Override
			public boolean matches(String valueToCheck) {
				if (valueToCheck.contains("d") || valueToCheck.contains("D")
						|| valueToCheck.contains("f") || valueToCheck.contains("F")
						|| valueToCheck.contains(" ")) {
					return false;
				}
				if ("-".equals(valueToCheck)) {
					return true;
				}
				if (valueToCheck.startsWith("-")) {
					valueToCheck = valueToCheck.substring(1);
				}

				if (valueToCheck.isEmpty()) {
					return true;
				} else if ((precision == 0) && valueToCheck.contains(".")) {
					return false;
				} else if (valueToCheck.contains(".") && valueToCheck.substring(valueToCheck.indexOf('.')).length() > precision + 1) {
					return false;
				} else if ((valueToCheck.contains(".") ? valueToCheck.length()-1 : valueToCheck.length()) > maxLength) {
					return false;
				} else if ((valueToCheck.contains(".")
						? valueToCheck.substring(0,valueToCheck.indexOf('.'))
						: valueToCheck).length() > maxLength - precision) {
					return false;

				} else {
					try {
						Double.parseDouble(valueToCheck);
						return true;
					} catch (NumberFormatException e) {
						return false;
					}
				}
			}
		};
	}
}
