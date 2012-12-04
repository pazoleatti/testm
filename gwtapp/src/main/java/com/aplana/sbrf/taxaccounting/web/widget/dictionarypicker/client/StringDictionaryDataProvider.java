package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.StringDictionaryAction;

/**
 * @author Vitalii Samolovskikh
 */
public class StringDictionaryDataProvider extends DictionaryDataProvider<StringDictionaryAction, String> {
	public StringDictionaryDataProvider(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected StringDictionaryAction createAction() {
		return new StringDictionaryAction();
	}

}
