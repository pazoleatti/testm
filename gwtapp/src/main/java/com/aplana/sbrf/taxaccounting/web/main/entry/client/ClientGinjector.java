package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutPagePresenter;
import com.aplana.sbrf.taxaccounting.web.module.about.client.ContactPagePresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListPresenter;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomePagePresenter;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter;
import com.google.web.bindery.event.shared.EventBus;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

@GinModules({ClientModule.class})
public interface ClientGinjector extends Ginjector {
	PlaceManager getPlaceManager();

	EventBus getEventBus();

	Provider<MainPagePresenter> getMainPagePresenter();

	/* Презенторы модулей */

	AsyncProvider<AboutPagePresenter> getAboutUsPresenter();

	AsyncProvider<ContactPagePresenter> getContactPresenter();

	AsyncProvider<HomePagePresenter> getHomePresenter();

	AsyncProvider<FormDataListPresenter> getFormDataListPresenter();

	AsyncProvider<FormDataPresenter> getFormDataPresenter();

	Provider<SignInPresenter> getSinInPresenter();
}