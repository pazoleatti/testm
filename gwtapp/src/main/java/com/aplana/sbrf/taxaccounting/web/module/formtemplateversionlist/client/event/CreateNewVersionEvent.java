package com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class CreateNewVersionEvent extends GwtEvent<CreateNewVersionEvent.MyHandler> {
    private int formTypeId;

    public CreateNewVersionEvent(int formTypeId) {
        this.formTypeId = formTypeId;
    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    public static void fire(HasHandlers source, int formTypeId) {
        source.fireEvent(new CreateNewVersionEvent(formTypeId));
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onCreateVersion(this);
    }

    public static interface MyHandler extends EventHandler {
        void onCreateVersion(CreateNewVersionEvent event);
    }

    private static final GwtEvent.Type<MyHandler> TYPE = new GwtEvent.Type<MyHandler>();

    public static GwtEvent.Type<MyHandler> getType() {
        return TYPE;
    }

    public int getFormTypeId() {
        return formTypeId;
    }
}
