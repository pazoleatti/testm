package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments.DeclarationDeclarationFilesCommentsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments.DeclarationFilesCommentsView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams.SubreportParamsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams.SubreportParamsView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.workflowdialog.DialogView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationDataPresenter.class, DeclarationDataPresenter.MyView.class,
				DeclarationDataView.class, DeclarationDataPresenter.MyProxy.class);
		bindSingletonPresenterWidget(DialogPresenter.class, DialogPresenter.MyView.class, DialogView.class);
        bindSingletonPresenterWidget(SourcesPresenter.class, SourcesPresenter.MyView.class, SourcesView.class);
        bindSingletonPresenterWidget(SubreportParamsPresenter.class, SubreportParamsPresenter.MyView.class, SubreportParamsView.class);
        bindSingletonPresenterWidget(DeclarationDeclarationFilesCommentsPresenter.class, DeclarationDeclarationFilesCommentsPresenter.MyView.class, DeclarationFilesCommentsView.class);
	}

}