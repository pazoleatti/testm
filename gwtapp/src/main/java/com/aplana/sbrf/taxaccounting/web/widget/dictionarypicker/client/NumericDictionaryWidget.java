package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.aplana.sbrf.taxaccounting.web.main.api.client.FormatUtils;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.AsyncDataProvider;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class NumericDictionaryWidget extends DictionaryPickerWidget<BigDecimal> {
	public NumericDictionaryWidget(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected DictionaryDataProvider<?, BigDecimal> createDataProvider(String dictionaryCode) {
		return new NumericDictionaryDataProvider(dictionaryCode);
	}

	@Override
	protected String valueToString(BigDecimal value) {
		if (value != null) {
			return FormatUtils.getSimpleNumberFormat().format(value);
		} else {
			return "";
		}
	}
}
