package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.web.main.api.client.FormatUtils;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.DictionaryPickerWidget;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.client.NumericDictionaryWidget;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

import java.math.BigDecimal;

/**
 * @author Vitalii Samolovskikh
 * @see DictionaryCell
 */
public class NumericDictionaryCell extends DictionaryCell<BigDecimal> {

	ColumnContext columnContext;

	public NumericDictionaryCell(String dictionaryCode, ColumnContext columnContext) {
		super(dictionaryCode);
		this.columnContext = columnContext;
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

	@Override
	public void onBrowserEvent(Context context, Element parent, BigDecimal value,
	                           NativeEvent event, ValueUpdater<BigDecimal> valueUpdater) {
		DataRow<Cell> dataRow = (DataRow<Cell>)context.getKey();
		if ((columnContext.getMode() == ColumnContext.Mode.EDIT_MODE)
				|| ((columnContext.getMode() != ColumnContext.Mode.READONLY_MODE)
				&& dataRow.getCell(columnContext.getColumn().getAlias()).isEditable())) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}
}
