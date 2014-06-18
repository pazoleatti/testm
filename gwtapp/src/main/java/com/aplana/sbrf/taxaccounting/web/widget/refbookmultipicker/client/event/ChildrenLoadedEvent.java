package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookUiTreeItem;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Событие после загрузки какой либо ветки
 * @author aivanov
 */
public class ChildrenLoadedEvent extends GwtEvent<ChildrenLoadedEvent.Handler> {

    private RefBookUiTreeItem uiTreeItem;

    public ChildrenLoadedEvent(RefBookUiTreeItem uiTreeItem) {
        this.uiTreeItem = uiTreeItem;
    }

    public static interface Handler extends EventHandler {
        void onChildrenLoaded(ChildrenLoadedEvent event);
    }

    private static final Type<Handler> TYPE = new Type<Handler>();

    public static void fire(HasHandlers source, RefBookUiTreeItem uiTreeItem) {
        source.fireEvent(new ChildrenLoadedEvent(uiTreeItem));
    }

    public static Type<Handler> getType() {
        return TYPE;
    }

    public ChildrenLoadedEvent() {
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onChildrenLoaded(this);
    }
}
