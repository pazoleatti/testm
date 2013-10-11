package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

public class ValidatedInputCell extends KeyPressableTextInputCell {

	ColumnContext columnContext;

	public ValidatedInputCell(final ColumnContext columnContext) {
		super(new SafeHtmlRenderer<String>() {
			@Override
			public SafeHtml render(String s) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				return builder.appendEscaped(columnContext.getColumn().getFormatter().format(s)).toSafeHtml();
			}

			@Override
			public void render(String s, SafeHtmlBuilder safeHtmlBuilder) {
					safeHtmlBuilder.appendEscaped(columnContext.getColumn().getFormatter().format(s));
			}
		});
		this.columnContext = columnContext;
	}

	@Override
	protected boolean checkInputtedValue(String value) {
		return columnContext.getColumn().getValidationStrategy().matches(value);
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, String value,
							   NativeEvent event, ValueUpdater<String> valueUpdater) {

		if (columnContext.getMode() == columnContext.mode.SUPER_EDIT_MODE) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		} else {
			@SuppressWarnings("unchecked")
			AbstractCell editableCell = ((DataRow<?>) context.getKey()).getCell(columnContext.getColumn().getAlias());
			if (DataRowEditableCellUtils.editMode(columnContext, editableCell)) {
				super.onBrowserEvent(context, parent, value, event, valueUpdater);
			}
		}
					
	}

}
