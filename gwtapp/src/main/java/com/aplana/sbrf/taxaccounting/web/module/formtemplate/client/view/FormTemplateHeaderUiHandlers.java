package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view;


import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.gwtplatform.mvp.client.UiHandlers;

public interface FormTemplateHeaderUiHandlers extends UiHandlers {
	void onRemoveButton(DataRow<HeaderCell> row);
	void onAddButton(DataRow<HeaderCell> row);
	void onAddNumberedHeaderButton(DataRow<HeaderCell> row);
	void onDataViewChanged();
}
