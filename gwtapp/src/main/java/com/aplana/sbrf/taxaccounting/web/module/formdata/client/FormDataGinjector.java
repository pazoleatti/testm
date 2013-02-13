package com.aplana.sbrf.taxaccounting.web.module.formdata.client;

import com.aplana.sbrf.taxaccounting.web.module.formdata.client.signers.SignersPresenter;
import com.google.gwt.inject.client.AsyncProvider;

public interface FormDataGinjector {

	AsyncProvider<FormDataPresenter> getFormDataPresenter();
	AsyncProvider<SignersPresenter> getSignersPresenter();
}