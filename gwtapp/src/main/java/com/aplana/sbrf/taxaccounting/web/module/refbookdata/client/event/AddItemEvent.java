package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Доаавление новой записи.
 * Используется в иерархическом спраоочнике
 * User: avanteev
 */
public class AddItemEvent extends
        GwtEvent<AddItemEvent.AddItemHandler> {


    private static final Type<AddItemHandler> TYPE = new Type<AddItemHandler>();

    public static Type<AddItemHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<AddItemHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(AddItemHandler handler) {
        handler.onAddItem(this);
    }

    public interface AddItemHandler extends EventHandler {
        /**
         * @param event
         */
        void onAddItem(AddItemEvent event);
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new AddItemEvent());
    }

}
