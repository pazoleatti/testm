package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.TextDictionaryWidget;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * @author Vitalii Samolovskikh
 * @see DictionaryCell
 */
public class TextDictionaryCell extends DictionaryCell<String> {

	ColumnContext columnContext;

	public TextDictionaryCell(String dictionaryCode, ColumnContext columnContext) {
		super(dictionaryCode);
		this.columnContext = columnContext;
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

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
	                           NativeEvent event, ValueUpdater<String> valueUpdater) {
		DataRow dataRow = (DataRow)context.getKey();
		if ((columnContext.getMode() == ColumnContext.Mode.EDIT_MODE)
				|| ((columnContext.getMode() != ColumnContext.Mode.READONLY_MODE)
				&& dataRow.getCell(columnContext.getColumn().getAlias()).isEditable())) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}
}
