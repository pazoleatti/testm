package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookHierDataUiHandlers extends UiHandlers {
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onSelectionChanged();
	void onRelevanceDateChanged();
    void setMode(FormMode mode);
}
