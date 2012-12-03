package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.user.cellview.client.CellTable;

public interface GenericCellTableResources extends CellTable.Resources {

	@Source(value = { CellTable.Style.DEFAULT_CSS, "GenericCellTableStyle.css" })
	CellTable.Style cellTableStyle();

}
