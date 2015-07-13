package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class ShowItemEvent extends GwtEvent<ShowItemEvent.ShowItemHandler> {

    private String dereferenceValue;
    private Long recordId;

    public ShowItemEvent(String dereferenceValue, Long recordId) {
        this.dereferenceValue = dereferenceValue;
        this.recordId = recordId;
    }

    public String getDereferenceValue() {
        return dereferenceValue;
    }

    public Long getRecordId() {
        return recordId;
    }

    private static final Type<ShowItemHandler> TYPE = new Type<ShowItemHandler>();

    public static Type<ShowItemHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<ShowItemHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ShowItemHandler handler) {
        handler.onShowItem(this);
    }

    public interface ShowItemHandler extends EventHandler {
        /**
         * Показывает выбранное в таблице значение
         * @param event
         */
        void onShowItem(ShowItemEvent event);
    }

    public static void fire(HasHandlers source, String dereferenceValue, Long recordId) {
        ShowItemEvent event = new ShowItemEvent(dereferenceValue, recordId);
        source.fireEvent(event);
    }
}
