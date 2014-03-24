package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookDataUiHandlers extends UiHandlers {
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onSelectionChanged();
	void onRelevanceDateChanged();
    void onBackClicked();
}
