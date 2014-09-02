package com.aplana.gwt.client.modal;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class OpenModalWindowEvent extends GwtEvent<OpenModalWindowEvent.OpenHandler> {

    public interface OpenHandler extends EventHandler {
        void onOpen(OpenModalWindowEvent event);
    }

    private static final Type<OpenHandler> TYPE = new Type<OpenHandler>();

    public static Type<OpenHandler> getType() {
        return TYPE;
    }

    public OpenModalWindowEvent() {
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new OpenModalWindowEvent());
    }

    @Override
    protected void dispatch(OpenHandler handler) {
        handler.onOpen(this);
    }

    @Override
    public Type<OpenHandler> getAssociatedType() {
        return getType();
    }

}
