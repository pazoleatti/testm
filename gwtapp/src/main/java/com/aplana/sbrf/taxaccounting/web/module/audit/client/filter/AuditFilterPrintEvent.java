package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class AuditFilterPrintEvent extends GwtEvent<AuditFilterPrintEvent.AuditFilterPrintHandler> {

    public interface AuditFilterPrintHandler extends EventHandler{
        void onPrintButtonClicked(AuditFilterPrintEvent event);
    }

    private static final Type<AuditFilterPrintHandler> TYPE = new Type<AuditFilterPrintHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new AuditFilterPrintEvent());
    }

    public static Type<AuditFilterPrintHandler> getType(){
        return TYPE;
    }

    @Override
    public Type<AuditFilterPrintHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(AuditFilterPrintHandler handler) {
        handler.onPrintButtonClicked(this);
    }
}
