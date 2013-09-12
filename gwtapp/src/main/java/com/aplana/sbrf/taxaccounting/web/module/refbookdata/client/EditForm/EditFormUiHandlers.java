package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.EditForm;

import com.gwtplatform.mvp.client.UiHandlers;

public interface EditFormUiHandlers extends UiHandlers {
	void onSaveClicked();
	void onCancelClicked();
	void valueChanged();
	void onRelevanceDateChanged();
}
