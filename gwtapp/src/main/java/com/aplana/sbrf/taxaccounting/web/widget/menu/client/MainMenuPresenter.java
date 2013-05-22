package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.MenuItem;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainMenuPresenter extends PresenterWidget<MainMenu>{
	
	public interface MyView extends View {	
		void setMenuItems(List<MenuItem> links);
	}
	
	private final DispatchAsync dispatchAsync;

	@Inject
	public MainMenuPresenter(EventBus eventBus, MainMenu view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	@Override
	protected void onReveal() {
		
		GetMainMenuAction action = new GetMainMenuAction();
		
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetMainMenuResult>() {
					@Override
					public void onSuccess(GetMainMenuResult result) {
						getView().setMenuItems(result.getMenuItems());
					}
				}, this));

		super.onReveal();
		
	}
	
}
