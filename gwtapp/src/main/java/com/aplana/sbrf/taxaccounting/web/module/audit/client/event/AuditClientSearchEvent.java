package com.aplana.sbrf.taxaccounting.web.module.audit.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class AuditClientSearchEvent extends GwtEvent<AuditClientSearchEvent.AuditFormSearchHandler> {
    public static interface AuditFormSearchHandler extends EventHandler{
        void onAuditFormSearchButtonClicked(AuditClientSearchEvent event);
    }

    private static final Type<AuditFormSearchHandler> TYPE = new Type<AuditFormSearchHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new AuditClientSearchEvent());
    }

    public static Type<AuditFormSearchHandler> getType(){
        return TYPE;
    }

    @Override
    public Type getAssociatedType() {
        return getType();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void dispatch(AuditFormSearchHandler handler) {
        handler.onAuditFormSearchButtonClicked(this);
    }
}
