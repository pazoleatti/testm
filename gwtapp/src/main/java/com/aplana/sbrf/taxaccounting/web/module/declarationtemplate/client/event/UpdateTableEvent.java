package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class UpdateTableEvent extends GwtEvent<UpdateTableEvent.MyHandler> {
    public interface MyHandler extends EventHandler {
        /**
         * @param event
         */
        void onUpdateTable(UpdateTableEvent event);
    }

    private String uuid;

    public UpdateTableEvent() {
    }

    public UpdateTableEvent(String uuid) {
        this.uuid = uuid;
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static Type<MyHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        UpdateTableEvent event = new UpdateTableEvent();
        source.fireEvent(event);
    }

    public static void fire(HasHandlers source, String uuid) {
        UpdateTableEvent event = new UpdateTableEvent(uuid);
        source.fireEvent(event);
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onUpdateTable(this);

    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    public String getUuid() {
        return uuid;
    }
}
