package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.workflowdialog.DialogPresenter;
import com.google.gwt.inject.client.AsyncProvider;

public interface FormDataGinjector {

	AsyncProvider<FormDataPresenter> getFormDataPresenter();
	AsyncProvider<SignersPresenter> getSignersPresenter();
	AsyncProvider<DialogPresenter> getDialogPresenter();
}