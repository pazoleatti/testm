package com.aplana.sbrf.taxaccounting.web.module.home.client;

import com.aplana.sbrf.taxaccounting.web.module.home.client.HomePagePresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class HomePageView extends ViewImpl implements MyView {
	interface Binder extends UiBinder<Widget, HomePageView> {
	}

	@UiField
	FlowPanel test;

	@Inject
	public HomePageView(final Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		super.setInSlot(slot, content);
	}

}