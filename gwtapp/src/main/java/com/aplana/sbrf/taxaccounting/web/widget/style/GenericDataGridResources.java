package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.DataGrid;

public interface GenericDataGridResources extends DataGrid.Resources {

	//@Source(value = { DataGrid.Style.DEFAULT_CSS, "GenericDataGridStyle.css" })
	@Source(value = {"GenericDataGridStyle.css" })
	DataGrid.Style dataGridStyle();

    @Source("cellTableLoading.gif")
    @ImageResource.ImageOptions(flipRtl = true, preventInlining = true)
    ImageResource dataGridLoading();

}
