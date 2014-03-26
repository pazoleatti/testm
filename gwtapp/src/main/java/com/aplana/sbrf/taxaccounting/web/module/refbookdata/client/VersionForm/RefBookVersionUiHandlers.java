package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.VersionForm;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookVersionUiHandlers extends UiHandlers {
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onSelectionChanged();
    void setMode(FormMode mode);
}
