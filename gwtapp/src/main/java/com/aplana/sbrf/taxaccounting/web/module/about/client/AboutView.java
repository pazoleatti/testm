package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class AboutView extends ViewImpl implements AboutPresenter.MyView {
	interface Binder extends UiBinder<Widget, AboutView> {
	}

	@UiField
	Panel version;

	@Inject
	public AboutView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		version.clear();
		if (content != null) {
			version.add(content);
		}
	}
}