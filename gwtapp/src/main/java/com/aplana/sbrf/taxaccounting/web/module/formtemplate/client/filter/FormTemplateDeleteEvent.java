package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class FormTemplateDeleteEvent extends GwtEvent<FormTemplateDeleteEvent.MyHandler> {

    @Override
    public Type<FormTemplateDeleteEvent.MyHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onDeleteCreate(this);
    }

    public static interface MyHandler extends EventHandler {
        void onDeleteCreate(FormTemplateDeleteEvent event);
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new FormTemplateDeleteEvent());
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static Type<MyHandler> getType() {
        return TYPE;
    }
}
