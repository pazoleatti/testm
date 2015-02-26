package com.aplana.sbrf.taxaccounting.web.module.lock.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Планировщик задач"
 *
 * @author dloshkarev
 *
 */
public class LockModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(LockListPresenter.class, LockListPresenter.MyView.class,
                LockListView.class, LockListPresenter.MyProxy.class);
	}
}