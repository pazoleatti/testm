package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.*;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор для меню "Руководство пользователя"
 *
 * @author Fail Mukhametdinov
 */
public class ManualMenuPresenter extends AbstractMenuPresenter<ManualMenuPresenter.MyView> {

    private static final int NOTIFICATION_UPDATE_TIME = 30 * 1000; //30s
    private DialogPresenter dialogPresenter;
    private MainMenuPresenter mainMenuPresenter;

    private Timer refreshTimer;

    @Inject
    public ManualMenuPresenter(EventBus eventBus, ManualMenuView view, DialogPresenter dialogPresenter, MainMenuPresenter mainMenuPresenter, DispatchAsync dispatchAsync) {
        super(eventBus, view, dispatchAsync);
        this.dialogPresenter = dialogPresenter;
        this.mainMenuPresenter = mainMenuPresenter;
    }

    @Override
    protected void onReveal() {
        GetManualMenuAction action = new GetManualMenuAction();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetManualMenuResult>() {
                    @Override
                    public void onSuccess(GetManualMenuResult result) {
                        getView().clearMenu();
                        if (result.canShowNotification()) {
                            NotificationMenuItem notificationMenuItem = new NotificationMenuItem();
                            notificationMenuItem.setScheduledCommand(new Scheduler.ScheduledCommand() {
                                @Override
                                public void execute() {
                                    showNotificationDialog();
                                }
                            });
                            getView().setNotificationMenuItem(notificationMenuItem);
                            updateNotificationCount();

                            refreshTimer = new Timer() {
                                @Override
                                public void run() {
                                    updateNotificationCount();
                                }
                            };
                            refreshTimer.scheduleRepeating(NOTIFICATION_UPDATE_TIME);
                        }
                        getView().setMenuItems(result.getMenuItems());
                    }
                }, this));
        super.onReveal();
    }

    public void showNotificationDialog() {
        dispatchAsync.execute(new UpdateNotificationStatusAction(), CallbackUtils
                .defaultCallbackNoLock(new AbstractCallback<UpdateNotificationStatusResult>() {
                    @Override
                    public void onSuccess(UpdateNotificationStatusResult result) {
                        getView().updateNotificationCount(0);
                        getView().selectNotificationMenuItem();
                        addToPopupSlot(dialogPresenter);
                    }
                }, ManualMenuPresenter.this));
    }

    public void updateNotificationCount() {
        GetNotificationCountAction action = new GetNotificationCountAction();
        action.setRolesHashCode(mainMenuPresenter.getRolesHashCode());
        dispatchAsync.execute(action, CallbackUtils
                .simpleCallback(new AbstractCallback<GetNotificationCountResult>() {
                    @Override
                    public void onSuccess(GetNotificationCountResult result) {
                        getView().updateNotificationCount(result.getNotificationCount());
                        if (result.isEditedRoles()) {
                            mainMenuPresenter.onReveal();
                        }
                    }
                }));
    }

    public interface MyView extends AbstractMenuPresenter.MyView {
        void setNotificationMenuItem(NotificationMenuItem item);

        void updateNotificationCount(int count);

        void selectNotificationMenuItem();

        void clearMenu();
    }
}
