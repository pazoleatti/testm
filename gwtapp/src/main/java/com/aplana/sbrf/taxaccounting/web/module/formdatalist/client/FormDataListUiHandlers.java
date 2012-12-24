package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface FormDataListUiHandlers extends UiHandlers{
	
	void onApplyFilter();

    void onCreateClicked();

	void onSortingChanged();

}
