package com.aplana.sbrf.taxaccounting.web.widget.cell.date;

import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.DateColumn;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.formdata.AbstractCell;
import com.aplana.sbrf.taxaccounting.web.widget.cell.ColumnContext;
import com.aplana.sbrf.taxaccounting.web.widget.cell.DataRowEditableCellUtils;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;

import java.util.Date;

public class DateMaskInputWithModesCell extends DateMaskInputCell {

	public DateMaskInputWithModesCell(final ColumnContext columnContext) {
		super(new SafeHtmlRenderer<String>() {
			@Override
			public SafeHtml render(String stringDate) {
				SafeHtmlBuilder builder = new SafeHtmlBuilder();
                String formattedDate = getFormattedDate(stringDate, columnContext);
				return builder.appendEscaped(columnContext.getColumn().getFormatter().format(formattedDate)).toSafeHtml();
			}

			@Override
			public void render(String stringDate, SafeHtmlBuilder safeHtmlBuilder) {
                String formattedDate = getFormattedDate(stringDate, columnContext);
				safeHtmlBuilder.appendEscaped(columnContext.getColumn().getFormatter().format(formattedDate));
			}
		}, columnContext, Formats.getById(getFormatId(columnContext)).getMask(), Formats.getById(getFormatId(columnContext)).getFormat());
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

    private static String getFormattedDate(String stringDate, ColumnContext columnContext){
        int formatId = getFormatId(columnContext);

        String formatById = Formats.getById(formatId).getFormat();
        Date date = DateTimeFormat.getFormat(formatById).parse(stringDate);

        // TODO будет ли вообще использоваться такой формат (aivanov)
        String format = formatById.replace("MMMM", Formats.getRussianMonthName(date.getMonth()));

        return DateTimeFormat.getFormat(format).format(date);
    }

    private static int getFormatId(ColumnContext columnContext){
        int formatId = ((DateColumn) columnContext.getColumn()).getFormatId();
        return formatId == Formats.NONE.getId() ? Formats.DD_MM_YYYY.getId() : formatId;
    }
}
