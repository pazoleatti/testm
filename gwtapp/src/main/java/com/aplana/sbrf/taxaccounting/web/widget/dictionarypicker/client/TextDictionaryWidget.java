package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.google.gwt.user.cellview.client.TextColumn;

/**
 * @author Vitalii Samolovskikh
 */
public class TextDictionaryWidget extends DictionaryPickerWidget<String> {
	public TextDictionaryWidget(String dictionaryCode) {
		super(dictionaryCode);
	}

	@Override
	protected String valueToString(String value) {
		return value;
	}

	protected DictionaryDataProvider<?, String> createDataProvider(String dictionaryCode) {
		return new StringDictionaryDataProvider(dictionaryCode);
	}
}
