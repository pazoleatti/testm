package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

public class ValidatedInputCell extends KeyPressableTextInputCell {

	ColumnContext columnContext;

	public interface ValidationStrategy {
		public boolean matches(String valueToCheck);
	}

	private ValidationStrategy overallFormValidationStrategy;

	public ValidatedInputCell(ValidationStrategy overallFormValidationStrategy, ColumnContext columnContext) {
		this.columnContext = columnContext;
		if (overallFormValidationStrategy != null) {
			this.overallFormValidationStrategy = overallFormValidationStrategy;
		} else {
			this.overallFormValidationStrategy = new DefaultValidationStrategy();
		}
	}

	@Override
	protected boolean checkInputtedValue(String value) {
		return overallFormValidationStrategy.matches(value);
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
