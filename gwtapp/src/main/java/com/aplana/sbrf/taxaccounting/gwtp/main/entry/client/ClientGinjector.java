package com.aplana.sbrf.taxaccounting.gwtp.main.entry.client;

import com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataListPresenter;
import com.aplana.sbrf.taxaccounting.gwtapp.client.FormDataPresenter;
import com.aplana.sbrf.taxaccounting.gwtp.control.singin.client.SingInPresenter;
import com.aplana.sbrf.taxaccounting.gwtp.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.gwtp.module.about.client.AboutPagePresenter;
import com.aplana.sbrf.taxaccounting.gwtp.module.about.client.ContactPagePresenter;
import com.aplana.sbrf.taxaccounting.gwtp.module.home.client.HomePagePresenter;
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
  
  
  Provider<SingInPresenter> getSinInPresenter();
  
}