package com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public class YearTreeItem  extends TreeItem{

	
	public YearTreeItem(int year) {
		super();
		Widget widget = new Label(String.valueOf(year));
		setWidget(widget);
	}

}
