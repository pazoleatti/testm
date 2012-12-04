package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryAction;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.shared.NumericDictionaryResult;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class NumericDictionaryProvider extends DictionaryDataProvider<NumericDictionaryAction, NumericDictionaryResult, BigDecimal> {
	public NumericDictionaryProvider(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected NumericDictionaryAction createAction() {
		return new NumericDictionaryAction();
	}
}
