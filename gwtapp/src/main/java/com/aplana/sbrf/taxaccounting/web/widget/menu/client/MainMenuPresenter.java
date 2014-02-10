package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import java.util.List;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.client.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.*;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class MainMenuPresenter extends PresenterWidget<MainMenu>{
	
	public interface MyView extends View {	
		void setMenuItems(List<MenuItem> links);
		void setNotificationMenuItem(NotificationMenuItem item);
		void updateNotificationCount(int count);
	}
	
	private final DispatchAsync dispatchAsync;

	private DialogPresenter dialogPresenter;

	private Timer refreshTimer;
	private static int NOTIFICATION_UPDATE_TIME = 5 * 60 * 1000; //5min

	@Inject
	public MainMenuPresenter(EventBus eventBus, MainMenu view, DialogPresenter dialogPresenter, final DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		this.dialogPresenter = dialogPresenter;
	}

	@Override
	protected void onReveal() {
		GetMainMenuAction action = new GetMainMenuAction();
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetMainMenuResult>() {
					@Override
					public void onSuccess(GetMainMenuResult result) {
						getView().setMenuItems(result.getMenuItems());
						getView().setNotificationMenuItem(new NotificationMenuItem(result.getNotificationMenuItemName(), MainMenuPresenter.this));

						refreshTimer = new Timer() {
							@Override
							public void run() {
								updateNotificationCount();
							}
						};
						refreshTimer.scheduleRepeating(NOTIFICATION_UPDATE_TIME);

						updateNotificationCount();
					}
				}, this));

		super.onReveal();
	}

	public void showNotificationDialog() {
		addToPopupSlot(dialogPresenter);
	}

	public void updateNotificationCount() {
		dispatchAsync.execute(new GetNotificationCountAction(), CallbackUtils
				.defaultCallbackNoLock(new AbstractCallback<GetNotificationCountResult>() {
					@Override
					public void onSuccess(GetNotificationCountResult result) {
						getView().updateNotificationCount(result.getNotificationCount());
					}
				}, MainMenuPresenter.this));
	}


	
}
