package com.aplana.sbrf.taxaccounting.web.widget.declarationparamsdialog.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.GINContextHolder;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.DialogBoxChangeVisibilityEvent;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

/**
 * Диалог ввода параметров уведомления
 *
 * @author Dmitriy Levykin
 */
public class DeclarationParamsDialogPresenter extends PresenterWidget<DeclarationParamsDialogPresenter.MyView> implements DeclarationParamsDialogUiHandlers {

    private ConfirmHandler confirmHandler;

    public interface MyView extends PopupView, HasUiHandlers<DeclarationParamsDialogUiHandlers> {
        void clearInput();
        Integer getPagesCount();
    }

    public DeclarationParamsDialogPresenter(MyView view) {
        super(GINContextHolder.getEventBus(), view);
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().clearInput();
        DialogBoxChangeVisibilityEvent.fire(this, true);
    }

    @Override
    public void setConfirmHandler(ConfirmHandler confirmHandler) {
        this.confirmHandler = confirmHandler;
    }

    @Override
    public void onConfirm() {
        hide();
        if (confirmHandler != null) {
            confirmHandler.onConfirm(getView().getPagesCount());
        }
    }

    @Override
    public void hide() {
        getView().hide();
        DialogBoxChangeVisibilityEvent.fire(this, false);
    }
}
