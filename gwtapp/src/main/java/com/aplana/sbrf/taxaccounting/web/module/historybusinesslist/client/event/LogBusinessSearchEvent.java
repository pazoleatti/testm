package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class LogBusinessSearchEvent extends GwtEvent<LogBusinessSearchEvent.MyHandler> {
    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onLogBusinessSearchButtonClicked(this);
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static Type<MyHandler> getType(){
        return TYPE;
    }

    public static interface MyHandler extends EventHandler {
        void onLogBusinessSearchButtonClicked(LogBusinessSearchEvent event);
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new LogBusinessSearchEvent());
    }
}
