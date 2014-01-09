package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;


import com.gwtplatform.mvp.client.UiHandlers;

public interface DeclarationListUiHandlers extends UiHandlers {

	void onSortingChanged();

    void onRangeChange(int start, int length);

    void onCreateClicked();
}
