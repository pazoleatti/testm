package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class HistoryPresenter extends
		PresenterWidget<HistoryView> {


	public static interface MyView extends PopupView{
	}

	@Inject
	public HistoryPresenter(final EventBus eventBus, final HistoryView view) {
		super(eventBus, view);
	}

	public void show() {
		getView().show();
	}

}
