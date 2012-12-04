package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.TextDictionaryWidget;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author Vitalii Samolovskikh
 */
public class TextDictionaryCell extends DictionaryCell<String> {
	public TextDictionaryCell(String dictionaryCode) {
		super(dictionaryCode);
	}

	protected DictionaryPickerWidget<String> createWidget(String dictionaryCode) {
		return new TextDictionaryWidget(dictionaryCode);
	}

	@Override
	public void render(Context context, String value, SafeHtmlBuilder sb) {
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
			s = value;
		}
		if (s != null) {
			sb.append(renderer.render(s));
		} else {
			// nbsp win(alt+255)
			sb.append(renderer.render(" "));
		}

	}
}
