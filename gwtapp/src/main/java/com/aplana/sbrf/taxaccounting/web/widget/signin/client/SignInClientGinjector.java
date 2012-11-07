package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.google.inject.Provider;

public interface SignInClientGinjector {

	Provider<SignInPresenter> getSinInPresenter();
	
}