package com.aplana.sbrf.taxaccounting.web.module.commonparameter.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class CommonParameterModule extends AbstractPresenterModule {
	
	@Override
	protected void configure() {
		bindPresenter(CommonParameterPresenter.class,
				CommonParameterPresenter.MyView.class, CommonParameterView.class,
				CommonParameterPresenter.MyProxy.class);
	}
}
