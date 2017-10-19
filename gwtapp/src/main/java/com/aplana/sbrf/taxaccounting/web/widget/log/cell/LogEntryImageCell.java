package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.GWTLogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class LogEntryImageCell extends AbstractCell<GWTLogEntry> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div style=\" text-align:center;\" >" +
				"<img src=\"{0}\" height=\"15\" width=\"15\">"+
				"</div>")
		SafeHtml cell(String url);
	}
	private static Templates templates = GWT.create(Templates.class);
	private static final String DEFAULT_URL = "resources/img/error-16.png";
	
	public LogEntryImageCell() {}
	
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
					   GWTLogEntry value, SafeHtmlBuilder sb) {
		if(value != null && value.getLevel() == LogLevel.ERROR){
			SafeHtml render = templates.cell(DEFAULT_URL);
			sb.append(render);
		}
	}
}
