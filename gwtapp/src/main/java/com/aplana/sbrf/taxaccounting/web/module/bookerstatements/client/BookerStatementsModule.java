package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client;

import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create.CreateBookerStatementsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.client.create.CreateBookerStatementsView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для Формы фильтрации бухгалтерской отчётности
 *
 * @author Dmitriy Levykin
 *
 */
public class BookerStatementsModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(BookerStatementsPresenter.class, BookerStatementsPresenter.MyView.class,
                BookerStatementsView.class, BookerStatementsPresenter.MyProxy.class);
        bindSingletonPresenterWidget(CreateBookerStatementsPresenter.class, CreateBookerStatementsPresenter.MyView.class, CreateBookerStatementsView.class);
    }
}