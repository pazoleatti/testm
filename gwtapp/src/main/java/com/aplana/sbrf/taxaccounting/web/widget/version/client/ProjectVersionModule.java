package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import com.google.inject.Singleton;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ProjectVersionModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindSingletonPresenterWidget(ProjectVersionPresenter.class, ProjectVersionPresenter.MyView.class, ProjectVersionView.class);

		bind(ProjectVersionView.Binder.class).in(Singleton.class);
	}

}
