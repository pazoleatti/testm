package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.main.api.client.FormatUtils;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.NumericDictionaryWidget;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class NumericDictionaryCell extends DictionaryCell<BigDecimal> {
	public NumericDictionaryCell(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected DictionaryPickerWidget<BigDecimal> createWidget(String dictionaryCode) {
		return new NumericDictionaryWidget(dictionaryCode);
	}

	protected String valueToString(BigDecimal value) {
		if(value!=null){
			return FormatUtils.getSimpleNumberFormat().format(value);
		} else {
			return "\u00A0";
		}
	}
}
