package com.aplana.sbrf.taxaccounting.web.module.admin.client.view;


import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateRowUiHandlers extends UiHandlers {
	void onAddButton();
	void onRemoveButton(DataRow row);
}
