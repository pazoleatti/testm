package com.aplana.sbrf.taxaccounting.web.module.refbooklist.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы списка справочников
 *
 * @author Stanislav Yasinskiy
 *
 */
public class RefBookListModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(RefBookListPresenter.class, RefBookListPresenter.MyView.class,
                RefBookListView.class, RefBookListPresenter.MyProxy.class);
	}
}