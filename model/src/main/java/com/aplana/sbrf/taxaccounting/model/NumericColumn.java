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
	public static final int MAX_PRECISION = 4;

	private int precision = 0;

	private String dictionaryCode;

	/**
	 * Возвращает значение точности числового столбца
	 * Точность столбца задаёт количество знаков после запятой, которые допустимы в данном столбце, для целых чисел используентся значение 0
	 *
	 * @return значение точности
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
			throw new IllegalArgumentException("Value " + precision + " is not supported for NumericAttribute");
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
}
