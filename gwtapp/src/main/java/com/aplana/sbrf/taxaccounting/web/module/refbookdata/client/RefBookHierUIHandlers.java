package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

/**
 * User: avanteev
 */
public interface RefBookHierUIHandlers extends UiHandlers {
    void onRelevanceDateChanged(Date relevanceDate);
    void setMode(FormMode mode);
    void cancelChanges();
    boolean isFormModified();
    void saveChanges();
    void onDeleteRowClicked();
    void searchButtonClicked();
    void onAddRowClicked();
    void onBackToRefBookAnchorClicked();
    void onBackClicked();
}
