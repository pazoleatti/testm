package com.aplana.sbrf.taxaccounting.web.main.api.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class FocusActionEvent extends GwtEvent<FocusActionEventHandler> {

    public static Type<FocusActionEventHandler> TYPE = new Type<FocusActionEventHandler>();

    private boolean focusEnabled;

    public FocusActionEvent(boolean focusEnabled) {
        this.focusEnabled = focusEnabled;
    }

    @Override
    public Type<FocusActionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FocusActionEventHandler handler) {
        handler.update(this);
    }

    public boolean isFocusEnabled() {
        return focusEnabled;
    }

    public void setFocusEnabled(boolean focusEnabled) {
        this.focusEnabled = focusEnabled;
    }
}
