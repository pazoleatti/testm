package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationDataPresenter.class, DeclarationDataPresenter.MyView.class,
				DeclarationDataView.class, DeclarationDataPresenter.MyProxy.class);
		bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);

	}

}