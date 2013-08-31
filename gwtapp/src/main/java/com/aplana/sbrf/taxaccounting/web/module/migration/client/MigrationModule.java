package com.aplana.sbrf.taxaccounting.web.module.migration.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы "Миграция исторических данных"
 *
 * @author Dmitriy Levykin
 *
 */
public class MigrationModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(MigrationPresenter.class, MigrationPresenter.MyView.class,
                MigrationView.class, MigrationPresenter.MyProxy.class);
	}
}