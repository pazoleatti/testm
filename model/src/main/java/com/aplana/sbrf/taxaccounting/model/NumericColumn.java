package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;
import java.math.BigDecimal;

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
	public static final int MAX_PRECISION = 8;
	
	/**
	 * Максимально допустимое колличество значений для числового столбца
	 * (ограничение налагается возможностями БД и деталями описания таблицы NUMERIC_VALUE)
	 */
	public static final int MAX_LENGTH = 15;

	private int precision = 0;

	private String dictionaryCode;
	
	private int maxLength = MAX_LENGTH;

	
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
		if (precision < 0 || precision > MAX_LENGTH) {
			throw new IllegalArgumentException("Value " + precision + " is not supported by 'precision' field");
		}
		this.precision = precision;
	}

	/**
	 * @return код справочника, связанного с данным столбцом.
	 */
	public String getDictionaryCode() {
		return dictionaryCode;
	}

	/**
	 * @param dictionaryCode код справочника, связанного с данным столбцом, или null, если со столбцом не связан ни
	 *                       один справочник.
	 */
	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
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
		this.maxLength = maxLength;
	}
}
