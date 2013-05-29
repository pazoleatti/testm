package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter.MyView;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class SignInView extends ViewImpl implements MyView{
	interface Binder extends UiBinder<Widget, SignInView> {
	}

	@UiField
	Label userName;

	@UiField
	Label roleAndDepartment;

	@Inject
	public SignInView(final Binder binder){
		initWidget(binder.createAndBindUi(this));
	}

	public void setUserName(String userName) {
		this.userName.setText(userName);
	}

	@Override
	public void setRoleAndDepartment(String roleAndDepartment) {
		this.roleAndDepartment.setText(roleAndDepartment);
	}

}