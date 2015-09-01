package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.notificationswindow.DialogPresenter;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.*;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор для главного меню
 */
public class MainMenuPresenter extends AbstractMenuPresenter<MainMenuPresenter.MyView> {

    @Inject
    public MainMenuPresenter(EventBus eventBus, MainMenuView view, DispatchAsync dispatchAsync) {
        super(eventBus, view, dispatchAsync);
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

    public interface MyView extends AbstractMenuPresenter.MyView {

    }
}