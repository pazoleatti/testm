package com.aplana.sbrf.taxaccounting.web.module.audit.client.archive;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.Date;

/**
 * User: avanteev
 */
public class AuditArchiveDialogEvent extends GwtEvent<AuditArchiveDialogEvent.AuditArchiveHandler> {
    private static final Type<AuditArchiveHandler> TYPE = new Type<AuditArchiveHandler>();

    private Date archiveDate;

    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    public static void fire(HasHandlers source, Date date) {
        AuditArchiveDialogEvent event = new AuditArchiveDialogEvent();
        event.setArchiveDate(date);
        source.fireEvent(event);
    }

    @Override
    public Type<AuditArchiveHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(AuditArchiveHandler handler) {
        handler.onAuditArchiveClickEvent(this);
    }

    public interface AuditArchiveHandler extends EventHandler{
        void onAuditArchiveClickEvent(AuditArchiveDialogEvent event);
    }

    public static Type<AuditArchiveHandler> getType(){
        return TYPE;
    }
}
