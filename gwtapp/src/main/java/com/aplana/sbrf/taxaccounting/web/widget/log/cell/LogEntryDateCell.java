package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка для вывода номера сообщения в журнале выполнения скриптов
 */
public class LogEntryDateCell extends AbstractCell<LogEntry> {

    private static final DateTimeFormat format = DateTimeFormat.getFormat("dd.MM.yyyy HH:mm:ss");

	interface Templates extends SafeHtmlTemplates {
		@Template("<span>{0}</span>")
		SafeHtml cell(SafeHtml value);
	}

	private static Templates templates = GWT.create(Templates.class);

	@Override
	public void render(Context context, LogEntry value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		SafeHtml safeValue = SafeHtmlUtils.fromString(value.getDate()!=null?format.format(value.getDate()):"");
		sb.append(templates.cell(safeValue));
	}
}
