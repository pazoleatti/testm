package com.aplana.sbrf.taxaccounting.web.widget.history;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.*;

public class HistoryPopup extends Composite {

	interface StyleWidgetUiBinder extends UiBinder<HTMLPanel, HistoryPopup> {
	}

	private static StyleWidgetUiBinder ourUiBinder = GWT.create(StyleWidgetUiBinder.class);
	private PopupPanel popup = new PopupPanel();

	@UiConstructor
	public HistoryPopup() {
		super();
		initWidget(ourUiBinder.createAndBindUi(this));
		popup.setWidget(this);
		popup.setAnimationEnabled(true);
		popup.setAutoHideEnabled(true);
	}
}