package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 * Нажатие на кнопку удаления строки
 */
public class DeleteItemEvent extends GwtEvent<DeleteItemEvent.DeleteItemHandler>{

    private static final GwtEvent.Type<DeleteItemHandler> TYPE = new GwtEvent.Type<DeleteItemHandler>();

    public static GwtEvent.Type<DeleteItemHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<DeleteItemHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(DeleteItemHandler handler) {
        handler.onDeleteItem(this);
    }

    public interface DeleteItemHandler extends EventHandler {
        void onDeleteItem(DeleteItemEvent event);
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new DeleteItemEvent());
    }
}
