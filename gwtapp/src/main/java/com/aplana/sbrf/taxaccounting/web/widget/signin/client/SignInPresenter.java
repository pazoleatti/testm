package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class SignInPresenter extends PresenterWidget<SignInView> implements SignInUiHandlers{
	
	public interface MyView extends View, HasUiHandlers<SignInUiHandlers> {
		void setUserLogin(String userLogin);
		void setUserName(String userName);
		void setRoleAndDepartment(String roleAndDepartment);
        void setHint(String hintForDepartment, String hintForUserName);
	}
	
	private final DispatchAsync dispatchAsync;
	private final PlaceManager placeManager;

	@Inject
	public SignInPresenter(EventBus eventBus, SignInView view, DispatchAsync dispatchAsync, PlaceManager placeManager) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		this.placeManager = placeManager;
		getView().setUiHandlers(this);
	}

	@Override
	protected void onReveal() {
		GetUserInfoAction action = new GetUserInfoAction();
		
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetUserInfoResult>() {
					@Override
					public void onSuccess(GetUserInfoResult result) {
						getView().setUserLogin(result.getUserLogon());
						getView().setUserName(result.getUserName());
						getView().setRoleAndDepartment(result.getRoleAnddepartment());
						getView().setHint(result.getRoleAnddepartment(), result.getHint());
					}
				}, this));

		super.onReveal();
	}

	@Override
	public void redirectLogoutUrl() {
		Window.Location.replace(GWT.getHostPageBaseURL()+"logout");
	}
}
