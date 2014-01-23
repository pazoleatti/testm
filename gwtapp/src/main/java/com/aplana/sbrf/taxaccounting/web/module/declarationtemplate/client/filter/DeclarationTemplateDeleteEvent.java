package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 * Удаление типа макета декларации
 */
public class DeclarationTemplateDeleteEvent extends GwtEvent<DeclarationTemplateDeleteEvent.MyHandler> {
    private static final Type<MyHandler> TYPE = new Type<MyHandler>();


    public static Type<MyHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new DeclarationTemplateDeleteEvent());
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onDelete(this);

    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    public static interface MyHandler extends EventHandler {
        void onDelete(DeclarationTemplateDeleteEvent event);
    }
}
