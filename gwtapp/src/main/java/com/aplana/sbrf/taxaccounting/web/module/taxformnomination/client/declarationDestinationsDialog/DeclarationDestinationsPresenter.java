package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasPopupSlot;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

/**
 * @author auldanov
 */
public class DeclarationDestinationsPresenter extends PresenterWidget<DeclarationDestinationsPresenter.MyView> implements DeclarationDestinationsUiHandlers {
    private final PlaceManager placeManager;
    private final DispatchAsync dispatchAsync;

    @Override
    public void onConfirm() {

    }

    public interface MyView extends PopupView, HasUiHandlers<DeclarationDestinationsUiHandlers>{

    }

    @Inject
    public DeclarationDestinationsPresenter(final EventBus eventBus, final MyView view, final DispatchAsync dispatchAsync, PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void initAndShowDialog(final HasPopupSlot slotForMe) {
        //getView().resetForm();
        slotForMe.addToPopupSlot(DeclarationDestinationsPresenter.this);
        //TODO логика загрузки данных

    }

}
