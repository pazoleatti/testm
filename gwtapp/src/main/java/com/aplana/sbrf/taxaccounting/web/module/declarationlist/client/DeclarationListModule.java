package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDView;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.creation.DeclarationCreationView;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download.DeclarationDownloadReportsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.download.DeclarationDownloadReportsView;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter.DeclarationFilterView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationListModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationListPresenter.class,
				DeclarationListPresenter.MyView.class,	DeclarationListView.class, DeclarationListPresenter.MyProxy.class);
		bindSingletonPresenterWidget(DeclarationFilterPresenter.class,
				DeclarationFilterPresenter.MyView.class, DeclarationFilterView.class);
		bindSingletonPresenterWidget(DeclarationCreationPresenter.class,
				DeclarationCreationPresenter.MyView.class, DeclarationCreationView.class);
        bindSingletonPresenterWidget(DeclarationDownloadReportsPresenter.class,
                DeclarationDownloadReportsPresenter.MyView.class, DeclarationDownloadReportsView.class);
    }
}
