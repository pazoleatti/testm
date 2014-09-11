package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.FormMode;
import com.gwtplatform.mvp.client.UiHandlers;

public interface EditFormUiHandlers extends UiHandlers {
	void onSaveClicked();
	void onCancelClicked();
	void valueChanged(String alias, Object value);
    void setMode(FormMode mode);
    void updateHistory();
}
