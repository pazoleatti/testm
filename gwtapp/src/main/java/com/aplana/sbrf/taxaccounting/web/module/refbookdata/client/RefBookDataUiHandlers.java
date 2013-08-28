package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client;

import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.RefBookDataRow;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Map;

public interface RefBookDataUiHandlers extends UiHandlers {
	void onCancelClicked();
	void onAddRowClicked();
	void onDeleteRowClicked();
	void onSelectionChanged();
	void onSaveClicked();
	void onValueChanged();
}
