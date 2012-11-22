package com.aplana.sbrf.taxaccounting.web.widget.cell;

import java.math.BigDecimal;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ReadOnlyNumericCell extends AbstractCell<BigDecimal> {

	private NumberFormat format;
	
	public ReadOnlyNumericCell(NumberFormat format) {
		this.format = format;
	}
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			BigDecimal value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		sb.appendEscaped(format.format(value));
	}
}
