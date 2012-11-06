package com.aplana.sbrf.taxaccounting.gwtp.control.singin.client;

import com.aplana.sbrf.taxaccounting.gwtp.control.singin.shared.GetUserInfoAction;
import com.aplana.sbrf.taxaccounting.gwtp.control.singin.shared.GetUserInfoResult;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class SingInPresenter extends PresenterWidget<SingIn>{
	
	public interface MyView extends View {
		void setUserName(String userName);
	}
	
	private final DispatchAsync dispatchAsync;

	@Inject
	public SingInPresenter(EventBus eventBus, SingIn view, DispatchAsync dispatchAsync) {

		super(eventBus, view);
		System.out.println("dfsdfsdfrer");
		this.dispatchAsync = dispatchAsync;
	}

	@Override
	protected void onReveal() {
		System.out.println("dfsdfsdf");
		
		GetUserInfoAction action = new GetUserInfoAction();
		
		dispatchAsync.execute(action, new AsyncCallback<GetUserInfoResult>(){

			@Override
			public void onFailure(Throwable caught) {
				caught.printStackTrace();
				getView().setUserName("Error");
			}

			@Override
			public void onSuccess(GetUserInfoResult result) {
				getView().setUserName(result.getUserName());
			}
			
		});
		super.onReveal();
		
	}
	
	@Override
	protected void onReset() {
		
		System.out.println("dfsdfsdf");
		// TODO Auto-generated method stub
		super.onReset();
	}
	
}
