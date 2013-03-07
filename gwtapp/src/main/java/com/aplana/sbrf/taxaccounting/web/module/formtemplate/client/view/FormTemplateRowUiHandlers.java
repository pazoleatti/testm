package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateRowUiHandlers extends UiHandlers {
	void onRemoveButton(DataRow row);
}
