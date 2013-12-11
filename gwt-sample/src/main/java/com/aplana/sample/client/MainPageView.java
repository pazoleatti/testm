package com.aplana.sample.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * @author Vitaliy Samolovskikh
 */
public class MainPageView extends ViewImpl implements MainPagePresenter.MyView {
	interface Binder extends UiBinder<Widget, MainPageView> {
	}

	@Inject
	public MainPageView(Binder binder) {
		initWidget(binder.createAndBindUi(this));
	}
}
