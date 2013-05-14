package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.google.gwt.cell.client.*;
import com.google.gwt.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

import java.util.*;

public class DateInputWithModesCell extends DateInputCell {

	ColumnContext columnContext;
	public static final String STORE_DATE_FORMAT = "dd.MM.yyyy";

	public DateInputWithModesCell(final ColumnContext columnContext) {
		super(new SafeHtmlRenderer<String>() {
			@Override
			public SafeHtml render(String s) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				Date date = DateTimeFormat.getFormat(STORE_DATE_FORMAT).parse(s);
				int formatId = ((DateColumn)columnContext.getColumn()).getFormatId();
				formatId = formatId == Formats.NONE.getId() ? Formats.DD_MM_YYYY.getId() : formatId;
				String format = Formats.getById(formatId).getFormat().replace("MMMM", Formats.getRussianMonthName(date.getMonth()));
				String formattedDate = DateTimeFormat.getFormat(format).format(date);
				return builder.appendEscaped(columnContext.getColumn().getFormatter().format(formattedDate)).toSafeHtml();
			}

			@Override
			public void render(String s, SafeHtmlBuilder safeHtmlBuilder) {
				Date date = DateTimeFormat.getFormat(STORE_DATE_FORMAT).parse(s);
				int formatId = ((DateColumn)columnContext.getColumn()).getFormatId();
				formatId = formatId == Formats.NONE.getId() ? Formats.DD_MM_YYYY.getId() : formatId;
				String format = Formats.getById(formatId).getFormat().replace("MMMM", Formats.getRussianMonthName(date.getMonth()));
				String formattedDate = DateTimeFormat.getFormat(format).format(date);
				safeHtmlBuilder.appendEscaped(columnContext.getColumn().getFormatter().format(formattedDate));
			}
		});
		this.columnContext = columnContext;
	}

	@Override
	public void onBrowserEvent(Context context, Element parent, Date value,
	                           NativeEvent event, ValueUpdater<Date> valueUpdater) {
		DataRow<Cell> dataRow = (DataRow<Cell>)context.getKey();
		if ((columnContext.getMode() == ColumnContext.Mode.EDIT_MODE) ||
				((columnContext.getMode() != ColumnContext.Mode.READONLY_MODE) &&
						dataRow.getCell(columnContext.getColumn().getAlias()).isEditable())) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}
}
