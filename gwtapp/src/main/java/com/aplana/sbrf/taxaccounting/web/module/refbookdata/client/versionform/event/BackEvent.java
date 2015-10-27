package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.versionform.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие для возврата на предыдущую форму со списком элементов
 */
public class BackEvent extends GwtEvent<BackEvent.BackHandler>{
    @Override
    public Type<BackHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(BackHandler handler) {
        handler.onBack(this);
    }

    public interface BackHandler extends EventHandler {
        /**
         * Имитирует нажатие на ссылку со списком элементов
         */
        void onBack(BackEvent event);
    }

    public static Type<BackHandler> getType() {
        return TYPE;
    }

    private static final Type<BackHandler> TYPE = new Type<BackHandler>();

    public static void fire(HasHandlers source) {
        source.fireEvent(new BackEvent());
    }
}
