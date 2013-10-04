package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class DeclarationFilterApplyEvent extends GwtEvent<DeclarationFilterApplyEvent.DeclarationFilterApplyHandler> {
    public static interface DeclarationFilterApplyHandler extends EventHandler{
        void onClickApply(DeclarationFilterApplyEvent event);
    }

    private static final Type<DeclarationFilterApplyHandler> TYPE = new Type<DeclarationFilterApplyHandler>();

    public static Type<DeclarationFilterApplyHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new DeclarationFilterApplyEvent());
    }

    @Override
    public Type<DeclarationFilterApplyHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(DeclarationFilterApplyHandler handler) {
        handler.onClickApply(this);
    }
}
