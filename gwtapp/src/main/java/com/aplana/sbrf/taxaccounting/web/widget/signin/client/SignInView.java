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

public class SignInView extends ViewWithUiHandlers<SignInUiHandlers> implements MyView{
	interface Binder extends UiBinder<Widget, SignInView> {
	}

	@UiField
	Label userName;

	@UiField
	Label roleAndDepartment;

	@UiField
	Anchor logout;

	@UiField
	FormPanel formPanelLogout;

	private String userLogin;

	@Inject
	public SignInView(final Binder binder){
		initWidget(binder.createAndBindUi(this));
		formPanelLogout.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event) {
				if (getUiHandlers() != null) {
					getUiHandlers().redirectHomePage();
				}
			}
		});
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
		doGet("controller/actions/clearAuthenticationCache/");
	}

	public void doGet(String url) {
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

		try {
			builder.setUser(userLogin);
			builder.setPassword("logout");
			builder.sendRequest(null, new RequestCallback() {
				public void onError(Request request, Throwable exception) {
					// ничего не делаем
				}

				public void onResponseReceived(Request request, Response response) {
					formPanelLogout.submit();
				}
			});

		} catch (RequestException e) {
			// ничего не делаем
		}
	}
}