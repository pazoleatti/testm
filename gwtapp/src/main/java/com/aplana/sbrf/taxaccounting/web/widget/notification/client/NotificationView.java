package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.web.widget.log.LogEntriesView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class NotificationView extends ViewImpl implements
		NotificationPresenter.MyView {

	interface Binder extends UiBinder<Widget, NotificationView> {
	}

	private final Widget widget;

	@Inject
	public NotificationView(Binder uiBinder) {
		widget = uiBinder.createAndBindUi(this);
	}

	@UiField
	LogEntriesView logEntries;

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setLogEntries(List<LogEntry> entries) {
		logEntries.setLogEntries(entries);
	}

	@Override
	public void setLogSize(int size) {
		// TODO Auto-generated method stub
		
	}

}
