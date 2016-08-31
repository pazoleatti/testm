package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.hierarchy;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface RefBookHierDataUiHandlers extends UiHandlers {
	void onSelectionChanged();
	void onRelevanceDateChanged();
    void setMode(FormMode mode);
    void onCleanEditForm();
    Long getRefBookId();
}
