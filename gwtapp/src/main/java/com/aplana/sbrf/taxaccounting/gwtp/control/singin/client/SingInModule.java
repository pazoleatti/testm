package com.aplana.sbrf.taxaccounting.gwtp.control.singin.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SingInModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		
		bindPresenterWidget(SingInPresenter.class, SingInPresenter.MyView.class,
				SingIn.class);

	}

}