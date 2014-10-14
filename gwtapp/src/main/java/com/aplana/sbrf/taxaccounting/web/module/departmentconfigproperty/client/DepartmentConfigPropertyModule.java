package com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DepartmentConfigPropertyModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenter(DepartmentConfigPropertyPresenter.class, DepartmentConfigPropertyPresenter.MyView.class,
                DepartmentConfigPropertyView.class, DepartmentConfigPropertyPresenter.MyProxy.class);
	}
}