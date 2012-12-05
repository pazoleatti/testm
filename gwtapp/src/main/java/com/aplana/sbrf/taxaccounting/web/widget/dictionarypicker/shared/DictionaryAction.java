package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;

/**
 * Базовый класс для действий получения значений справочника. Содержит информацию для пэджинга и паттерн поиска для
 * фильтрации значений справочника.
 *
 * @param <VT> тип значения: {@link String}, {@link java.math.BigDecimal}.
 * @author Vitalii Samolovskikh
 */
public abstract class DictionaryAction<VT extends Serializable> extends UnsecuredActionImpl<DictionaryResult<VT>> {
	private int offset;
	private int max;
	private String dictionaryCode;
	private String searchPattern = null;

	/**
	 * @return смещение относительно начала справочника для пэйджинга.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @param offset смещение относительно начала справочника для пэйджинга.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @return максимальное количество возвращаемых значений.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * @param max максимальное количество возвращаемых значений.
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * @return код справочника
	 */
	public String getDictionaryCode() {
		return dictionaryCode;
	}

	/**
	 * @param dictionaryCode код справочника
	 */
	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}

	/**
	 * @return паттерн поиска
	 */
	public String getSearchPattern() {
		return searchPattern;
	}

	/**
	 * @param searchPattern паттерн поиска
	 */
	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}
}
