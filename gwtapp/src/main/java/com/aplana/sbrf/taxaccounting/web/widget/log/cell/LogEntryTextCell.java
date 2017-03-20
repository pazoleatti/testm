package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 * Ячейка для вывода сообщений журнала выполнения скрипта в списке
 */
public abstract class LogEntryTextCell extends AbstractCell<LogEntry> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<span style=\"{0}\">{1}</span>")
		SafeHtml cell(SafeStyles styles, SafeHtml value);
	}    	

	private static Templates templates = GWT.create(Templates.class);

    public abstract String getStringValue(LogEntry value);

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context, LogEntry value, SafeHtmlBuilder sb) {
		if (value == null) {
			return;
		}
		SafeHtml safeValue = SafeHtmlUtils.fromString(getStringValue(value));
		
		String color;
		if (LogLevel.ERROR.equals(value.getLevel())) {
			color = "Red";
		} else if (LogLevel.WARNING.equals(value.getLevel())) {
			color = "DarkViolet";
		} else {
			color = "Green";
		}
		SafeStyles styles = SafeStylesUtils.forTrustedColor(color);
		SafeHtml rendered = templates.cell(styles, safeValue);
		sb.append(rendered);
	}
}
