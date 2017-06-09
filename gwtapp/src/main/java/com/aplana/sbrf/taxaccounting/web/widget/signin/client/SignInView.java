package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter.MyView;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.http.client.*;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;

public class SignInView extends ViewWithUiHandlers<SignInUiHandlers> implements MyView{
	interface Binder extends UiBinder<Widget, SignInView> {
	}

	@UiField
	Label userName;

	@UiField
	Label roleAndDepartment;

	@UiField
	Anchor logout;

	private String userLogin;

	@Inject
	public SignInView(final Binder binder){
		initWidget(binder.createAndBindUi(this));
	}

	@Override
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
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

	@UiHandler("logout")
	public void onLogoutClicked(ClickEvent event) {
		logout();
	}

	private void clearAuthenticationCache() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "controller/actions/clearAuthenticationCache");
		try {
			builder.setUser(userLogin);
			builder.setPassword("logout"+(new Date()).getTime());
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					getUiHandlers().redirectHomePage();
				}

				public void onResponseReceived(Request request, Response response) {
					getUiHandlers().redirectHomePage();
				}
			});
		} catch (RequestException e) {
			// ничего не делаем
		}
	}

	private void logout() {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "j_spring_security_logout");
		try {
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// ничего не делаем
				}

				public void onResponseReceived(Request request, Response response) {
					clearAuthenticationCache();
				}
			});

		} catch (RequestException e) {
			// ничего не делаем
		}
	}

}