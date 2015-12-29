package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookDataUiHandlers extends UiHandlers {
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onRelevanceDateChanged();
    void setMode(FormMode mode);
    void onBackClicked();
    void saveChanges();
    void cancelChanges();
    boolean isFormModified();
    void sendQuery();
    void onSearchClick();
    void onBackToRefBookAnchorClicked();
    void onPrintClicked(String reportName);
}
