package com.aplana.sbrf.taxaccounting.gwtapp.client;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

public class FormListView extends ViewImpl {

	private final String html = "<div>Тут покажем список налоговых форм, доступных для редактирования</div>";
	
	private final HTMLPanel panel = new HTMLPanel(html);
	
	@Override
	public Widget asWidget() {
		// TODO Auto-generated method stub
		return panel;
	}

}
