package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookPickerWidgetUiHandlers extends UiHandlers, HasValue<Long>{
	
	void init(long refBookAttrId, String filter, Date date1, Date date2);
	
	void rangeChanged(int startIndex, int max);
	
	void searche();
	void versionChange();
	void onSelectionChange();
	void clearValue();
	
	String getDereferenceValue();

}
