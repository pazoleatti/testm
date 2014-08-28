package com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class CreateNewDTVersionEvent  extends GwtEvent<CreateNewDTVersionEvent.MyHandler> {
    private int typeId;

    public CreateNewDTVersionEvent(int typeId) {
        this.typeId = typeId;
    }

    @Override
    public GwtEvent.Type<MyHandler> getAssociatedType() {
        return getType();
    }

    public static void fire(HasHandlers source, int typeId) {
        source.fireEvent(new CreateNewDTVersionEvent(typeId));
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onCreateVersion(this);
    }

    public interface MyHandler extends EventHandler {
        void onCreateVersion(CreateNewDTVersionEvent event);
    }

    private static final GwtEvent.Type<MyHandler> TYPE = new GwtEvent.Type<MyHandler>();

    public static GwtEvent.Type<MyHandler> getType() {
        return TYPE;
    }

    public int getTypeId() {
        return typeId;
    }
}
