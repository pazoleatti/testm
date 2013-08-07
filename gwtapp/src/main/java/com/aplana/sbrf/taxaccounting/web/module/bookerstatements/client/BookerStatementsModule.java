package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 *
 */
public class BookerStatementsModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(BookerStatementsPresenter.class, BookerStatementsPresenter.MyView.class,
                BookerStatementsView.class, BookerStatementsPresenter.MyProxy.class);
	}
}