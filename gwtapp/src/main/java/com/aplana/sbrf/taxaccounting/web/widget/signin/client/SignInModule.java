package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class SignInModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		
		bindPresenterWidget(SignInPresenter.class, SignInPresenter.MyView.class,
				SignIn.class);

	}

}