package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка для вывода номера сообщения в журнале выполнения скриптов
 */
public class LogEntryIndexCell extends AbstractCell<LogEntry> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span>{0}</span>")
		SafeHtml cell(SafeHtml value);
	}    	

	private static Templates templates = GWT.create(Templates.class);

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, LogEntry value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		SafeHtml safeValue = SafeHtmlUtils.fromString(String.valueOf(context.getIndex() + 1));
		sb.append(templates.cell(safeValue));
	}
}
