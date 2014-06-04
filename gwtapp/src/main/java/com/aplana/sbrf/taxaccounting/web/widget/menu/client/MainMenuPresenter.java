package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationCountAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetNotificationCountResult;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор для главного меню
 */
public class MainMenuPresenter extends AbstractMenuPresenter<MainMenuPresenter.MyView> {

    private static int NOTIFICATION_UPDATE_TIME = 5 * 60 * 1000; //5min
    private DialogPresenter dialogPresenter;

    private Timer refreshTimer;

    @Inject
    public MainMenuPresenter(EventBus eventBus, MainMenuView view, DialogPresenter dialogPresenter, DispatchAsync dispatchAsync) {
        super(eventBus, view, dispatchAsync);
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
                        if (result.getNotificationMenuItemName() != null) {
                            NotificationMenuItem notificationMenuItem = new NotificationMenuItem(result.getNotificationMenuItemName());
                            notificationMenuItem.setScheduledCommand(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    showNotificationDialog();
                                }
                            });
                            getView().setNotificationMenuItem(notificationMenuItem);

                            refreshTimer = new Timer() {
                                @Override
                                public void run() {
                                    updateNotificationCount();
                                }
                            };
                            refreshTimer.scheduleRepeating(NOTIFICATION_UPDATE_TIME);

                            updateNotificationCount();
                        }
                    }
                }, this));

        super.onReveal();
    }

    public void showNotificationDialog() {
        getView().selectNotificationMenuItem();
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

    public interface MyView extends AbstractMenuPresenter.MyView {
        void setNotificationMenuItem(NotificationMenuItem item);

        void updateNotificationCount(int count);

        void selectNotificationMenuItem();
    }
}
