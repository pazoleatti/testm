package com.aplana.sbrf.taxaccounting.web.module.home.client;

import com.aplana.sbrf.taxaccounting.web.module.home.client.HomePagePresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class HomePageView extends ViewImpl implements MyView {
	interface Binder extends UiBinder<Widget, HomePageView> {
	}

	@UiField
	FlowPanel test;

	private final Widget widget;

	@Inject
	public HomePageView(final Binder binder) {
		widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setInSlot(Object slot, Widget content) {
		super.setInSlot(slot, content);
	}

}