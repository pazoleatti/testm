package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class NumericDictionaryDataProvider extends DictionaryDataProvider<NumericDictionaryAction, BigDecimal> {
	public NumericDictionaryDataProvider(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected NumericDictionaryAction createAction() {
		return new NumericDictionaryAction();
	}
}
