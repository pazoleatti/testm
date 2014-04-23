package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class RootLoadedEvent extends GwtEvent<RootLoadedEvent.RootLoadedHandler> {

    public static interface RootLoadedHandler extends EventHandler {
        void onRootLoaded(RootLoadedEvent event);
    }

    private static final Type<RootLoadedHandler> TYPE = new Type<RootLoadedHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new RootLoadedEvent());
    }

    public static Type<RootLoadedHandler> getType() {
        return TYPE;
    }

    public RootLoadedEvent() {
    }

    @Override
    public Type<RootLoadedHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(RootLoadedHandler handler) {
        handler.onRootLoaded(this);
    }
}
