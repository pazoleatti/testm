package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие нажатия на кнопку отмены на виджете редактирования
 */
public class CancelEvent extends GwtEvent<CancelEvent.CancelFormHandler> {
    private Long previousId;
    private static final Type<CancelFormHandler> TYPE = new Type<CancelFormHandler>();

    @Override
    public Type<CancelFormHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(CancelFormHandler handler) {
        handler.onCancelForm(this);
    }

    public interface CancelFormHandler extends EventHandler {
        void onCancelForm(CancelEvent event);
    }

    public static Type<CancelFormHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source, Long previousId) {
        CancelEvent event = new CancelEvent();
        event.previousId = previousId;
        source.fireEvent(event);
    }

    public Long getPreviousId() {
        return previousId;
    }
}
