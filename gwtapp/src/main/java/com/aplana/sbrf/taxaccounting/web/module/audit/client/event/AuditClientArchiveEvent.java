package com.aplana.sbrf.taxaccounting.web.module.audit.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 * Событие, вызывающееся при архивировании журнала событий.
 */
public class AuditClientArchiveEvent extends GwtEvent<AuditClientArchiveEvent.AuditClientArchiveHandler> {

    @Override
    public Type<AuditClientArchiveHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(AuditClientArchiveHandler handler) {
        handler.onAuditArchiveButtonClick(new AuditClientArchiveEvent());
    }

    private static final Type<AuditClientArchiveHandler> TYPE = new Type<AuditClientArchiveHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new AuditClientArchiveEvent());
    }

    public static Type<AuditClientArchiveHandler> getType(){
        return TYPE;
    }

    public static interface AuditClientArchiveHandler  extends EventHandler{
        void onAuditArchiveButtonClick(AuditClientArchiveEvent event);
    }
}
