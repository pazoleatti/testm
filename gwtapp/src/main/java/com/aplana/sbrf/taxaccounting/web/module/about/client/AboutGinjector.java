package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.google.gwt.inject.client.AsyncProvider;

public interface AboutGinjector {

	AsyncProvider<AboutPagePresenter> getAboutUsPresenter();

	AsyncProvider<ContactPagePresenter> getContactPresenter();
}