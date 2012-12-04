package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;

/**
 * @author Vitalii Samolovskikh
 */
public abstract class DictionaryAction<VT extends Serializable> extends UnsecuredActionImpl<DictionaryResult<VT>> {
	private int offset;
	private int max;
	private String dictionaryCode;
	private String searchPattern = null;

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public String getDictionaryCode() {
		return dictionaryCode;
	}

	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}

	public String getSearchPattern() {
		return searchPattern;
	}

	public void setSearchPattern(String searchPattern) {
		this.searchPattern = searchPattern;
	}
}
