package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface EditFormUiHandlers extends UiHandlers {
	void onSaveClicked(boolean isEditButtonClicked);
	void onCancelClicked();
	void valueChanged(String alias, Object value);
    void setMode(FormMode mode);
    String getTitle();
    boolean isVersioned();
    void setIsFormModified(boolean isFormModified);
}
