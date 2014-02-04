package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;
import java.util.List;

public interface RefBookMultiPickerViewUiHandlers extends UiHandlers {
	
	void init(long refBookAttrId, String filter, Date relevanceDate);
	
	void rangeChanged(int startIndex, int max);

    void onSort(Integer columnIndex, boolean isSortAscending);
	
	void searche();

	void versionChange();
}
