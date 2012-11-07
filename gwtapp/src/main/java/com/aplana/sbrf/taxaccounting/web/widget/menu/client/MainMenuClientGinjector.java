package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.google.inject.Provider;

public interface MainMenuClientGinjector {

	Provider<MainMenuPresenter> getMainMenuPresenter();
	
}