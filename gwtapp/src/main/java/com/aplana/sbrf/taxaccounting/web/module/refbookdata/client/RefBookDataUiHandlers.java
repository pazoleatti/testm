package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Map;

public interface RefBookDataUiHandlers extends UiHandlers {
	void onCancelClicked();
	void onAddRowClicked();
	void onSelectionChanged(Long recordId);
	void onSaveClicked(Map<String, Object > values);
}
