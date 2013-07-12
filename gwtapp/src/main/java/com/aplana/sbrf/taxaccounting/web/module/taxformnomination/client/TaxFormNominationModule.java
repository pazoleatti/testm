package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

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
	}

}