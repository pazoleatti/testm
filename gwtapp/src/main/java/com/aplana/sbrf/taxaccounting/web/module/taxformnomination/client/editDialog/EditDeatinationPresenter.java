package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.editDialog;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;


public class EditDeatinationPresenter extends PresenterWidget<EditDeatinationPresenter.MyView> implements EditDestinationUiHandlers {

    private final PlaceManager placeManager;
    private final DispatchAsync dispatchAsync;

    public interface MyView extends PopupView, HasUiHandlers<EditDestinationUiHandlers> {
    }

    @Inject
    public EditDeatinationPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    public void onConfirm() {
        //TODO логика
        getView().hide();
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe) {
        //TODO логика загрузки данных
        slotForMe.addToPopupSlot(EditDeatinationPresenter.this);
    }


}
