package com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class DeclarationFilterCreateEvent extends GwtEvent<DeclarationFilterCreateEvent.DeclarationFilterCreateHandler> {

    public static interface DeclarationFilterCreateHandler extends EventHandler{
        void onCreateClick(DeclarationFilterCreateEvent event);
    }

    private static final Type<DeclarationFilterCreateHandler> TYPE = new Type<DeclarationFilterCreateHandler>();

    public static Type<DeclarationFilterCreateHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new DeclarationFilterCreateEvent());
    }

    @Override
    public Type<DeclarationFilterCreateHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(DeclarationFilterCreateHandler handler) {
        handler.onCreateClick(this);
    }
}
