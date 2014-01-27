package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * @author auldanov
 */
public class DeclarationDestinationsDialogOpenEvent extends GwtEvent<DeclarationDestinationsDialogOpenEvent.EditDestinationDialogOpenHandler> {
    public static interface EditDestinationDialogOpenHandler extends EventHandler {
        void onClickEditDeclarationDestination(DeclarationDestinationsDialogOpenEvent event);
    }

    private static final Type<EditDestinationDialogOpenHandler> TYPE = new Type<EditDestinationDialogOpenHandler>();

    public static Type<EditDestinationDialogOpenHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new DeclarationDestinationsDialogOpenEvent());
    }


    public DeclarationDestinationsDialogOpenEvent() {
    }

    @Override
    protected void dispatch(EditDestinationDialogOpenHandler handler) {
        handler.onClickEditDeclarationDestination(this);
    }

    @Override
    public Type<EditDestinationDialogOpenHandler> getAssociatedType() {
        return getType();
    }
}
