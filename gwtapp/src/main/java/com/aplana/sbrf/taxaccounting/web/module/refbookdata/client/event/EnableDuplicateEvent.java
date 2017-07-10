package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class EnableDuplicateEvent extends GwtEvent<EnableDuplicateEvent.EnableDuplicateHandler> {

    public static final GwtEvent.Type<EnableDuplicateHandler> TYPE = new GwtEvent.Type<EnableDuplicateHandler>();

    private boolean enableDuplicate;

    public EnableDuplicateEvent(boolean enableDuplicate) {
        this.enableDuplicate = enableDuplicate;
    }

    public interface EnableDuplicateHandler extends EventHandler {
        void onSelectTableRow(EnableDuplicateEvent event);
    }

    @Override
    public Type<EnableDuplicateHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EnableDuplicateHandler handler) {
        handler.onSelectTableRow(this);
    }

    public boolean isEnableDuplicate() {
        return enableDuplicate;
    }

    public void setEnableDuplicate(boolean enableDuplicate) {
        this.enableDuplicate = enableDuplicate;
    }
}
