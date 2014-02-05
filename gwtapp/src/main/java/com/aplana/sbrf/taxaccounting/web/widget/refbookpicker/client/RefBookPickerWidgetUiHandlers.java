package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.google.gwt.user.client.ui.HasValue;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

public interface RefBookPickerWidgetUiHandlers extends UiHandlers, HasValue<Long>{
	
	void init(long refBookAttrId, String filter, Date relevanceDate);
	
	void rangeChanged(int startIndex, int max);
    void onSort(Integer columnIndex, boolean isSortAscending);
	
	void searche();
	void versionChange();
	void onSelectionChange();

    void clearValue();
	
	String getDereferenceValue();

    String getOtherDereferenceValue(String alias);

    String getOtherDereferenceValue(Long id);

}
