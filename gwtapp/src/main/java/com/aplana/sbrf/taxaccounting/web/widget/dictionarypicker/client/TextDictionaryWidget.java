package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

/**
 * @author Vitalii Samolovskikh
 * @see DictionaryPickerWidget
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
