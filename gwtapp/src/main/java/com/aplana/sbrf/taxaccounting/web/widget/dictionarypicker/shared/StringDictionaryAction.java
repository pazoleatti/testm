package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared;

/**
 * Запрос для получения элементов справочника передающий фильтр, 
 * границы выборки и код справочника.
 * @author Eugene Stetsenko
 *
 */
public class StringDictionaryAction extends DictionaryAction<StringDictionaryResult, String> {
	private String filter;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

}
