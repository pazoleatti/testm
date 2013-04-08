package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client;

import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.creationdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.filter.FilterPresenter;
import com.google.gwt.inject.client.AsyncProvider;

public interface FormDataListGinjector {

	AsyncProvider<FormDataListPresenter> getFormDataListPresenter();
	AsyncProvider<FilterPresenter> getFilterPresenter();
	AsyncProvider<DialogPresenter> getCreationDialogPresenter();

}