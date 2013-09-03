package com.aplana.sbrf.taxaccounting.web.widget.cell;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
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
			public SafeHtml render(String stringDate) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
				Date date = DateTimeFormat.getFormat(STORE_DATE_FORMAT).parse(stringDate);
				int formatId = ((DateColumn)columnContext.getColumn()).getFormatId();
				formatId = formatId == Formats.NONE.getId() ? Formats.DD_MM_YYYY.getId() : formatId;
				String format = Formats.getById(formatId).getFormat().replace("MMMM", Formats.getRussianMonthName(date.getMonth()));
				String formattedDate = DateTimeFormat.getFormat(format).format(date);
				return builder.appendEscaped(columnContext.getColumn().getFormatter().format(formattedDate)).toSafeHtml();
			}

			@Override
			public void render(String stringDate, SafeHtmlBuilder safeHtmlBuilder) {
				Date date = DateTimeFormat.getFormat(STORE_DATE_FORMAT).parse(stringDate);
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
		@SuppressWarnings("unchecked")
		AbstractCell editableCell = ((DataRow<?>) context.getKey()).getCell(columnContext.getColumn().getAlias());
		if (DataRowEditableCellUtils.editMode(columnContext, editableCell)) {
			super.onBrowserEvent(context, parent, value, event, valueUpdater);
		}
	}

	private String getFormattedDate(Date date){
		final String dateShortStart = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT).format(date);

		int startDayIndex = dateShortStart.lastIndexOf('-');
		int startMonthIndex = dateShortStart.indexOf('-');

		String startDate =  dateShortStart.substring(startDayIndex + 1, dateShortStart.length()) + '.' +
				dateShortStart.substring(startMonthIndex + 1, startDayIndex) + '.' +
				dateShortStart.substring(0, startMonthIndex);

		return startDate;
	}
}
