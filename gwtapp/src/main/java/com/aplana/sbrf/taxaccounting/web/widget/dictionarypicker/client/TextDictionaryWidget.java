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

	protected TextColumn<DictionaryItem<String>> createValueColumn() {
		return new TextColumn<DictionaryItem<String>>() {
				@Override
				public String getValue(DictionaryItem<String> object) {
					return object.getValue();
				}
			};
	}
}
