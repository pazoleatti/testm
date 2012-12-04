package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.io.Serializable;

/**
 * @author Vitalii Samolovskikh
 */
public abstract class DictionaryAction<R extends DictionaryResult<VT>, VT extends Serializable> extends UnsecuredActionImpl<R> {
	private Integer offset;
	private Integer max;
	private String dictionaryCode;

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public Integer getMax() {
		return max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public String getDictionaryCode() {
		return dictionaryCode;
	}

	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}
}
