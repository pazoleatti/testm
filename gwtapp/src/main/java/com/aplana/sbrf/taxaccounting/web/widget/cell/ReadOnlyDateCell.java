package com.aplana.sbrf.taxaccounting.web.widget.cell;

import java.util.Date;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class ReadOnlyDateCell extends AbstractCell<Date> {

	DateTimeFormat format;
	
	public ReadOnlyDateCell(DateTimeFormat format) {
		this.format = format;
	}
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			Date value, SafeHtmlBuilder sb) {
		if (value == null) {
	        return;
	    }
		String convertedDate = format.format(value);
		sb.appendEscaped(convertedDate);
		
	}

}
