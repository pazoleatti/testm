package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.FormatUtils;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 * @see DictionaryPickerWidget
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
