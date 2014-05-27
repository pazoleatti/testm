package com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для Формы просмотра бухгалтерской отчётности
 *
 * @author lhaziev
 *
 */
public class BookerStatementsDataModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(BookerStatementsDataPresenter.class, BookerStatementsDataPresenter.MyView.class,
                BookerStatementsDataView.class, BookerStatementsDataPresenter.MyProxy.class);
	}
}