package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter.MyView;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class SignInView extends ViewImpl implements MyView{
	interface Binder extends UiBinder<Widget, SignInView> {
	}

	private final Widget widget;

	@UiField
	Label userName;

	@Inject
	public SignInView(final Binder binder){
		this.widget = binder.createAndBindUi(this);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public void setUserName(String userName) {
		this.userName.setText(userName);
	}
	
}