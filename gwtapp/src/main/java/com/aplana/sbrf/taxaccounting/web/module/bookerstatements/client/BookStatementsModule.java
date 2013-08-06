package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Загрузка бухгалтерской отчётности"
 *
 * @author Dmitriy Levykin
 *
 */
public class BookStatementsModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(BookStatementsPresenter.class, BookStatementsPresenter.MyView.class,
                BookStatementsView.class, BookStatementsPresenter.MyProxy.class);
	}
}