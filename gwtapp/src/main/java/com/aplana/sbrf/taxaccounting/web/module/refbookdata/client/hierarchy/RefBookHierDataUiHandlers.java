package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookHierDataUiHandlers extends UiHandlers {
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onSelectionChanged();
	void onRelevanceDateChanged();
}
