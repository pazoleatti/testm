package com.aplana.sbrf.taxaccounting.web.widget.log;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.TaActionException;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

public class ThrowableWidget extends Composite implements ThrowableView {

	private static Binder uiBinder = GWT.create(Binder.class);

	interface Binder extends UiBinder<Widget, ThrowableWidget> {
	}

	@UiField
	HTMLPanel htmlPanel;

	@UiField
	Label showTextLabel;

	@UiField
	TextArea text;

	public ThrowableWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		htmlPanel.getElement().getStyle().setMargin(1, Style.Unit.EM);
	}

	@Override
	public void setThrowable(Throwable throwable) {
		if (throwable != null) {
			if (throwable instanceof TaActionException
					&& ((TaActionException) throwable).getTrace() != null) {
				text.setText(((TaActionException) throwable).getTrace());
			} else {
				StackTraceElement[] trace = throwable.getStackTrace();
				StringBuilder sb = new StringBuilder();
				for (StackTraceElement stackTraceElement : trace) {
					sb.append(stackTraceElement.toString()).append("\n");
				}
				text.setText(sb.toString());
			}

			text.setVisible(false);
			showTextLabel.setVisible(true);

		} else {
			showTextLabel.setVisible(false);
			text.setVisible(false);
			text.setText("");
		}
	}

	@UiHandler("showTextLabel")
	void onShowTextLabelClicked(ClickEvent event) {
		showTextLabel.setVisible(false);
		text.setVisible(true);
	}

}
