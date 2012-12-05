package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.TextDictionaryWidget;

/**
 * @author Vitalii Samolovskikh
 * @see DictionaryCell
 */
public class TextDictionaryCell extends DictionaryCell<String> {
	public TextDictionaryCell(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected DictionaryPickerWidget<String> createWidget(String dictionaryCode) {
		return new TextDictionaryWidget(dictionaryCode);
	}

	@Override
	protected String valueToString(String value) {
		if (value != null) {
			return value;
		} else {
			return "\u00A0";
		}
	}
}
