package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;

public class ThrowableWidget extends Composite implements ThrowableView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ThrowableWidget> {
	}

	@UiField
	HTMLPanel htmlPanel;

	@UiField
	Label message;
	
	@UiField
	TextArea text;

	public ThrowableWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		htmlPanel.getElement().getStyle().setMargin(1, Style.Unit.EM);
	}

	@Override
	public void setThrowable(Throwable throwable) {
		if (throwable != null) {
			text.setVisible(true);
			StackTraceElement[] trace = throwable.getStackTrace();
			StringBuilder sb = new StringBuilder();
			for (StackTraceElement stackTraceElement : trace) {
				sb.append(stackTraceElement.toString()).append("\n");
			}
			text.setText(sb.toString());
			message.setText(throwable.getLocalizedMessage());
		} else {
			text.setVisible(false);
			text.setText("");
			message.setText("");
		}
	}

}
