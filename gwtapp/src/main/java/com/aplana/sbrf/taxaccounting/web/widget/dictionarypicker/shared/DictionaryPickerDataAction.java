package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
/**
 * Запрос для получения элементов справочника передающий фильтр, 
 * границы выборки и код справочника.
 * @author Eugene Stetsenko
 *
 */
public class DictionaryPickerDataAction extends UnsecuredActionImpl<DictionaryPickerDataResult> {
	private String filter;
	private Integer start;
	private Integer offset;
	private String dictionaryCode;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getOffset() {
		return offset;
	}

	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	public String getDictionaryCode() {
		return dictionaryCode;
	}

	public void setDictionaryCode(String dictionaryCode) {
		this.dictionaryCode = dictionaryCode;
	}
}
