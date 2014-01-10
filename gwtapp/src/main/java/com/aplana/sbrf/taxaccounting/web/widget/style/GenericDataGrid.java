package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.CustomScrollPanel;
import com.google.gwt.user.client.ui.HeaderPanel;

public class GenericDataGrid<T> extends DataGrid<T>{

	public GenericDataGrid() {
		super(15, GWT.<GenericDataGridResources>create(GenericDataGridResources.class));
        HeaderPanel headerPanel = (HeaderPanel) getWidget();
        final CustomScrollPanel scrollPanel = (CustomScrollPanel ) headerPanel.getContentWidget();
        scrollPanel.getWidget().addStyleName("AplanaScrollPanel");
	}
}
