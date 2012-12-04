package com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client;

import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.cellview.client.TextColumn;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 */
public class NumericDictionaryWidget extends DictionaryPickerWidget<BigDecimal> {
	public NumericDictionaryWidget(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected TextColumn<DictionaryItem<BigDecimal>> createValueColumn() {
		return new TextColumn<DictionaryItem<BigDecimal>>() {
				@Override
				public String getValue(DictionaryItem<BigDecimal> object) {
					return NumberFormat.getFormat("0.#").format(object.getValue());
				}
			};
	}
}
