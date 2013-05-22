package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ProjectVersionView extends ViewImpl implements ProjectVersionPresenter.MyView {

	interface Binder extends UiBinder<Widget, ProjectVersionView> {
	}

	@UiField
	HasText projectVersion;
	
	@Inject
	public ProjectVersionView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setProjectVersion(String projectVersion) {
		this.projectVersion.setText(projectVersion);
	}

}
