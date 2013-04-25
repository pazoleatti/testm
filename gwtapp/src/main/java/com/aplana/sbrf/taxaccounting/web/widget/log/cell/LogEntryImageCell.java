package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class LogEntryImageCell extends AbstractCell<LogEntry> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div style=\" text-align:center;\" >" +
				"<img src=\"{0}\" height=\"15\" width=\"15\">"+
				"</div>")
		SafeHtml cell(String url);
	}
	private static Templates templates = GWT.create(Templates.class);
	private static final String DEFAULT_URL = "resources/img/error-16.png";
	
	public LogEntryImageCell(){
		
	}
	
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			LogEntry value, SafeHtmlBuilder sb) {
		if(value.getLevel() == LogLevel.ERROR){
			//SafeHtml safeValue = AbstractImagePrototype.create(Resources.INSTANCE.error()).getSafeHtml();
			SafeHtml render = templates.cell(DEFAULT_URL);
			sb.append(render);
		}
		
	}

}
