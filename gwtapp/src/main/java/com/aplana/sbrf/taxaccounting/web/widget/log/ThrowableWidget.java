package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class ThrowableWidget extends Composite implements ThrowableView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ThrowableWidget> {
	}

	@UiField
	Label showTextLabel;

	@UiField
	TextArea text;

	public ThrowableWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		text.getElement().setAttribute("wrap","off");
	}

	@Override
	public void setThrowable(Throwable throwable) {
		text.setText("");
		text.setVisible(false);
		if (throwable != null) {
			if (throwable instanceof TaActionException) {
				if (((TaActionException) throwable).getTrace() != null){
					text.setText(((TaActionException) throwable).getTrace());
					showTextLabel.setVisible(true);
				} else {
					showTextLabel.setVisible(false);
				}
			} else {
				StackTraceElement[] trace = throwable.getStackTrace();
				StringBuilder sb = new StringBuilder();
				for (StackTraceElement stackTraceElement : trace) {
					sb.append(stackTraceElement.toString()).append("\n");
				}
				text.setText(sb.toString());
				showTextLabel.setVisible(true);
			}
		} else {
			showTextLabel.setVisible(false);	
		}
	}

	@UiHandler("showTextLabel")
	void onShowTextLabelClicked(ClickEvent event) {
		showTextLabel.setVisible(false);
		text.setVisible(true);
	}

	
}

