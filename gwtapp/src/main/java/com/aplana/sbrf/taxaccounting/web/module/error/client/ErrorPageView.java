package com.aplana.sbrf.taxaccounting.web.module.error.client;

import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorPagePresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.log.ThrowableView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ErrorPageView extends ViewImpl implements MyView {

	interface Binder extends UiBinder<Widget, ErrorPageView> {
	}
	
	private final Widget widget;
	
	@UiField
	HasText message;
	
	@UiField
	ThrowableView throwableView;

	@Inject
	public ErrorPageView(Binder binder) {
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setMessage(String text) {
		message.setText(text);
		
	}

	@Override
	public void setStackTrace(Throwable throwable) {
		throwableView.setThrowable(throwable);
		
	}

}
