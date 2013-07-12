package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;

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
	private String dictionaryCode;
	private int maxLength = MAX_LENGTH;

	/**
	 * Получить код справочника, связанного с данным столбцов.
	 * 
	 * Обращаю внимание, что поскольку значение любой ячейки в налоговой может быть изменено из скриптов,
	 * то использование справочника не гарантирует того, что в столбце могут оказаться только значения, перечисленные в справочнике,
	 * автоматических проверок такой целосостности не производится.
	 * 
	 * В качестве значения, записываемого в столбец используется {@link DictionaryItem#getValue() значение} элемента справочника, 
	 * а не его {@link DictionaryItem#getName() описание}. 
	 * 
	 * @see {@link DictionaryManager}
	 * 
	 * @return код справочника, или null, если с данным столбцов не связан справочник
	 */
	public String getDictionaryCode() {
		return dictionaryCode;
	}

	/**
	 * Задать справочник, связанный с данным столбцом
	 * 
	 * @param dictionaryCode код справочника
	 */
	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}

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
