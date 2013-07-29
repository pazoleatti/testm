package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Реализация {@link Column}, предназначенная для представления столбцов со строковыми данными
 * 
 * Для строковых столбцов существует возможность задать справочник, значения из которого могут использоваться для заполнения столбца
 * @author dsultanbekov
 */
public class StringColumn extends Column  implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Максимально допустимое значение поля {@link #setMaxLength}
	 * (Ограничение накладывается параметрами таблицы STRING_VALUE в БД)
	 */
	public static final int MAX_LENGTH = 1000;

	private int maxLength = MAX_LENGTH;

	/**
	 * Получить максимально допустимую длину строки в этом столбце
	 * По умолчанию значение равно {@link #MAX_LENGTH} 
	 * @return максимальная длина строки
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * Задать максимально допустимую длину строки
	 * Значение должно быть больше нуля и меньше либо равно, чем {@link #MAX_LENGTH -  предел, задаваемый БД
	 * По умолчанию значение равно {@link #MAX_LENGTH}
	 * @param maxLength максимальная длина строки
	 * @throws IllegalArgumentException если maxLength меньше или равно нулю, или если оно больше, чем {#link MAX_LENGTH}
	 */
	public void setMaxLength(int maxLength) {
		if (maxLength <= 0) {
			throw new IllegalArgumentException("maxLength must be greater than 0");
		} else if (maxLength > MAX_LENGTH) {
			throw new IllegalArgumentException("maxLength must be lower than " + MAX_LENGTH);
		}		
		this.maxLength = maxLength;
	}

	@Override
	public ValidationStrategy getValidationStrategy() {
		return new ValidationStrategy() {
			@Override
			public boolean matches(String valueToCheck) {
				return valueToCheck != null ? valueToCheck.length() <= maxLength : true;
			}
		};

	}
}
