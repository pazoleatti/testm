package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.DataGrid;

public class GenericDataGrid<T> extends DataGrid<T>{
	
	public GenericDataGrid() {
		super(15, GWT.<GenericDataGridResources> create(GenericDataGridResources.class));
		this.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
	}
}
