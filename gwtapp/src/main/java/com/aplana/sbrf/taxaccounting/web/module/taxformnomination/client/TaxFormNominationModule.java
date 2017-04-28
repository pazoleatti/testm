package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog.DeclarationDestinationsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog.DeclarationDestinationsView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Назначение форм и деклараций"
 *
 * @author Stanislav Yasinskiy
 *
 */
public class TaxFormNominationModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(TaxFormNominationPresenter.class, TaxFormNominationPresenter.MyView.class,
				TaxFormNominationView.class, TaxFormNominationPresenter.MyProxy.class);
        bindSingletonPresenterWidget(DeclarationDestinationsPresenter.class, DeclarationDestinationsPresenter.MyView.class, DeclarationDestinationsView.class);
    }

}