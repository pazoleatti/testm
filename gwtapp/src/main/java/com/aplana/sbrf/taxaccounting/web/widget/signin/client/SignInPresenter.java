package com.aplana.sbrf.taxaccounting.web.widget.signin.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SignInPresenter extends PresenterWidget<SignInView>{
	
	public interface MyView extends View {
		void setUserName(String userName);
		void setRoleAndDepartment(String roleAndDepartment);
        void setHint(String value);
	}
	
	private final DispatchAsync dispatchAsync;

	@Inject
	public SignInPresenter(EventBus eventBus, SignInView view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	@Override
	protected void onReveal() {
		
		GetUserInfoAction action = new GetUserInfoAction();
		
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetUserInfoResult>() {
					@Override
					public void onSuccess(GetUserInfoResult result) {
						getView().setUserName(result.getUserName());
						getView().setRoleAndDepartment(result.getRoleAnddepartment());
						getView().setHint(result.getHint());
					}
				}, this));

		super.onReveal();
		
	}
	
}
