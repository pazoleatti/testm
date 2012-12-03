package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.user.cellview.client.DataGrid;

public interface GenericDataGridResources extends DataGrid.Resources {

	@Source(value = { DataGrid.Style.DEFAULT_CSS, "GenericDataGridStyle.css" })
	DataGrid.Style dataGridStyle();

}
