package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.sendQueryDialog;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SendQueryAction;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.shared.SendQueryResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

public class DialogPresenter extends PresenterWidget<DialogPresenter.MyView> implements DialogUiHandlers {

    private final DispatchAsync dispatchAsync;

    public interface MyView extends PopupView, HasUiHandlers<DialogUiHandlers> {
        void clearInput();

        String getComment();
    }

    @Inject
    public DialogPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().clearInput();
    }

    @Override
    public void onConfirm() {
        String text = getView().getComment();
        if ("".equals(text.trim())) {
            Dialog.warningMessage("Ошибка", "Опишите изменения, которые необходимо внести в справочник");
        } else {
            SendQueryAction action = new SendQueryAction();
            action.setMessage(getView().getComment());
            dispatchAsync.execute(action, new AbstractCallback<SendQueryResult>() {
                @Override
                public void onSuccess(SendQueryResult result) {
                    LogAddEvent.fire(DialogPresenter.this, result.getUuid());
                    getView().hide();
                }
            });
        }
    }
}
