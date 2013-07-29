package com.aplana.sbrf.taxaccounting.web.module.home.client;

import com.aplana.sbrf.taxaccounting.web.module.home.client.HomePagePresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerWidget;
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
	
	@UiField(provided=true) 
	RefBookPickerWidget picker;

	@Inject
	public HomePageView(final Binder binder) {
		picker = new RefBookPickerWidget(2l);
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setInSlot(Object slot, IsWidget content) {
		super.setInSlot(slot, content);
	}

}