package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetMainMenuResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор для главного меню
 */
public class MainMenuPresenter extends AbstractMenuPresenter<MainMenuPresenter.MyView> {

    private int rolesHashCode;

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
                        rolesHashCode = result.getRolesHashCode();
                        getView().setMenuItems(result.getMenuItems());
                    }
                }, this));
        super.onReveal();
    }

    public int getRolesHashCode() {
        return rolesHashCode;
    }

    public interface MyView extends AbstractMenuPresenter.MyView {

    }
}