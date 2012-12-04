package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryResult;

/**
 * @author Vitalii Samolovskikh
 */
public class StringDictionaryProvider extends DictionaryDataProvider<StringDictionaryAction, StringDictionaryResult, String> {
	private String filter=null;

	public StringDictionaryProvider(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected StringDictionaryAction createAction() {
		StringDictionaryAction action = new StringDictionaryAction();
		if (filter!=null && !filter.isEmpty()) {
			action.setFilter(filter);
		}
		return action;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
}
