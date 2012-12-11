package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class ThrowableWidget extends Composite implements ThrowableView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ThrowableWidget> {
	}

	@UiField
	HasText message;
	
	@UiField
	HasText text;

	public ThrowableWidget() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void setThrowable(Throwable throwable) {
		if (throwable != null) {
			StackTraceElement[] trace = throwable.getStackTrace();
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement stackTraceElement : trace) {
				sb.append(stackTraceElement.toString()).append("\n");
			}
			text.setText(sb.toString());
			message.setText(throwable.getLocalizedMessage());
		} else {
			text.setText("");
			message.setText("");
		}
	}

}
