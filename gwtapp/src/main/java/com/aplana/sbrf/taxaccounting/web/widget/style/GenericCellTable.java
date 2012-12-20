package com.aplana.sbrf.taxaccounting.web.widget.style;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;

public class GenericCellTable<T> extends CellTable<T>{
	
	public GenericCellTable() {
		super(20, GWT.<GenericCellTableResources> create(GenericCellTableResources.class));
		this.setStyleName("genericTable");
	}
}
