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

	@UiField
	Label userName;

	@UiField
	Label roleAndDepartment;

	@Inject
	public SignInView(final Binder binder){
		initWidget(binder.createAndBindUi(this));
	}

	@Override
    public void setUserName(String userName) {
		this.userName.setText(userName);
	}

	@Override
	public void setRoleAndDepartment(String roleAndDepartment) {
		this.roleAndDepartment.setText(roleAndDepartment);
	}

    @Override
    public void setHint(String hintForDepartment, String hintForUserName) {
        this.roleAndDepartment.setTitle(hintForDepartment);
        this.userName.setTitle(hintForUserName);
    }

}