package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewImpl;

public class HistoryView extends PopupViewImpl implements
		HistoryPresenter.MyView {

	interface Binder extends UiBinder<PopupPanel, HistoryView> {
	}

	private final PopupPanel widget;

	@Inject
	public HistoryView(EventBus eventBus, Binder uiBinder) {
		super(eventBus);
		widget = uiBinder.createAndBindUi(this);
		widget.setAutoHideEnabled(true);
		widget.setAnimationEnabled(true);
	}

	@Override
	public PopupPanel asWidget() {
		return widget;
	}



}
