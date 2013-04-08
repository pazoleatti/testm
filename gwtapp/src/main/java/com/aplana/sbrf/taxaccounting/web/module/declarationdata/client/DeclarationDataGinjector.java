package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.google.gwt.inject.client.AsyncProvider;

public interface DeclarationDataGinjector {
	AsyncProvider<DeclarationDataPresenter> getDeclarationDataPresenter();
	AsyncProvider<DialogPresenter> getReasonDialogPresenter();
}