package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ProjectVersionModule extends AbstractPresenterModule {

	@Override
	protected void configure() {
		bindPresenterWidget(ProjectVersionPresenter.class, ProjectVersionPresenter.MyView.class, ProjectVersionView.class);
	}

}
