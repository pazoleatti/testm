package com.aplana.sbrf.taxaccounting.web.widget.menu.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetManualMenuAction;
import com.aplana.sbrf.taxaccounting.web.widget.menu.shared.GetManualMenuResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор для меню "Руководство пользователя"
 *
 * @author Fail Mukhametdinov
 */
public class ManualMenuPresenter extends AbstractMenuPresenter<ManualMenuPresenter.MyView> {

    @Inject
    public ManualMenuPresenter(EventBus eventBus, ManualMenuView view, DispatchAsync dispatchAsync) {
        super(eventBus, view, dispatchAsync);
    }

    @Override
    protected void onReveal() {
        GetManualMenuAction action = new GetManualMenuAction();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetManualMenuResult>() {
                    @Override
                    public void onSuccess(GetManualMenuResult result) {
                        getView().setMenuItems(result.getMenuItems());
                    }
                }, this));
        super.onReveal();
    }

    public interface MyView extends AbstractMenuPresenter.MyView {
    }
}
