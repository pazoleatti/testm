package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client;

import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.editDialog.EditDeatinationPresenter;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.editDialog.EditDestinationView;
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
        bindSingletonPresenterWidget(EditDeatinationPresenter.class, EditDeatinationPresenter.MyView.class, EditDestinationView.class);

    }

}