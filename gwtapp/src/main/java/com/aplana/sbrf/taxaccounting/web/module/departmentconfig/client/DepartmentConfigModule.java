package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Модуль для формы настроек подразделений
 *
 * @author Dmitriy Levykin
 *
 */
public class DepartmentConfigModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(DepartmentConfigPresenter.class, DepartmentConfigPresenter.MyView.class,
                DepartmentConfigView.class, DepartmentConfigPresenter.MyProxy.class);
	}
}