package com.aplana.sbrf.taxaccounting.web.widget.log.cell;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.web.widget.log.cell.resourceimage.Resources;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class LogEntryImageCell extends AbstractCell<LogEntry> {

	interface Templates extends SafeHtmlTemplates {
		@SafeHtmlTemplates.Template("<div style=\" text-align:center;\" >{0}</div>")
		SafeHtml cell(SafeHtml value);
	}
	private static Templates templates = GWT.create(Templates.class);
	
	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			LogEntry value, SafeHtmlBuilder sb) {
		if(value.getLevel() == LogLevel.ERROR){
			SafeHtml safeValue = AbstractImagePrototype.create(Resources.INSTANCE.error()).getSafeHtml();
			SafeHtml render = templates.cell(safeValue);
			sb.append(render);
		}
		
	}

}
