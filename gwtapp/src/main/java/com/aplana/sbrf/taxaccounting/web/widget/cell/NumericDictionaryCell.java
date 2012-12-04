package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.NumericDictionaryWidget;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

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

	@Override
	public void render(Context context, BigDecimal value, SafeHtmlBuilder sb) {
		// Get the view data.
		Object key = context.getKey();
		String viewData = getViewData(key);
		if (viewData != null && viewData.equals(value)) {
			clearViewData(key);
			viewData = null;
		}

		String s = null;
		if (viewData != null) {
			s = viewData;
		} else if (value != null) {
			s = NumberFormat.getDecimalFormat().format(value);
		}
		if (s != null) {
			sb.append(renderer.render(s));
		} else {
			// nbsp win(alt+255)
			sb.append(renderer.render(" "));
		}

	}
}
