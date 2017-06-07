package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create.MoveToCreateListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.move_to_create.MoveToCreateListView;
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
		bindSingletonPresenterWidget(MoveToCreateListPresenter.class, MoveToCreateListPresenter.MyView.class, MoveToCreateListView.class);
    }
}
