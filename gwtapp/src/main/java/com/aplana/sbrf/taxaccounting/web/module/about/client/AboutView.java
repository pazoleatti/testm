package com.aplana.sbrf.taxaccounting.web.module.about.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class AboutView extends ViewImpl implements AboutPresenter.MyView {
	interface Binder extends UiBinder<Widget, AboutView> {
	}

	@Inject
	public AboutView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}
}