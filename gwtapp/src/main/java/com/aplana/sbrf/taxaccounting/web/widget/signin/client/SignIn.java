package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInPresenter.MyView;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.ViewImpl;

public class SignIn extends ViewImpl implements IsWidget, MyView{
	interface Binder extends UiBinder<Widget, SignIn> {
	}

	private final Widget widget;
	
	private final DispatchAsync dispatchAsync;

	@UiField
	Label userName;
	
	@Inject
	public SignIn(final Binder binder, final DispatchAsync dispatchAsync){
		this.widget = binder.createAndBindUi(this);
		this.dispatchAsync = dispatchAsync;
		init();
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	public void setUserName(String userName) {
		this.userName.setText(userName);
	}
	

	protected void init() {
		System.out.println("dfsdfsdf");
		
		GetUserInfoAction action = new GetUserInfoAction();
		
		dispatchAsync.execute(action, new AsyncCallback<GetUserInfoResult>(){

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				setUserName("Error");
			}

			@Override
			public void onSuccess(GetUserInfoResult result) {
				setUserName("Пользователь: " + result.getUserName());
			}
			
		});
		
	}

	
}