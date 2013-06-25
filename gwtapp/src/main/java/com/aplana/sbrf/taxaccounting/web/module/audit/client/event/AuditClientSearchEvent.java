package com.aplana.sbrf.taxaccounting.web.module.audit.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class AuditClientSearchEvent extends GwtEvent<AuditClientSearchEvent.MyHandler> {
    public static interface MyHandler extends EventHandler{
        void onAuditFormSearchButtonClicked(AuditClientSearchEvent event);
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new AuditClientSearchEvent());
    }

    public static Type<MyHandler> getType(){
        return TYPE;
    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onAuditFormSearchButtonClicked(this);
    }
}
