package com.aplana.sbrf.taxaccounting.web.widget.notification.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
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
	Button button;

	@UiHandler("button")
	void onClick(ClickEvent e) {
		Window.alert("Hello!");
	}

	public void setText(String text) {
		button.setText(text);
	}

	public String getText() {
		return button.getText();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

}
