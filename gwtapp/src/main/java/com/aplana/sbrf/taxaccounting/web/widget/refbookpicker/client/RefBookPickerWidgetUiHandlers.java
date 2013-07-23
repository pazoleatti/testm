package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookPickerWidgetUiHandlers extends UiHandlers, HasValue<Long>{
	
	
	
	void rangeChanged(int startIndex, int max);
	
	void onSearchPatternChange();
	void onVersionChange();

}
