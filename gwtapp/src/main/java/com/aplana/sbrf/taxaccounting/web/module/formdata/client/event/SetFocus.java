package com.aplana.sbrf.taxaccounting.web.module.formdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by auldanov on 01.04.2014.
 */
public class SetFocus extends GwtEvent<SetFocus.SetFocusHandler> {

    public interface SetFocusHandler extends EventHandler {
        void onSetFocus(SetFocus event);
    }

    private Long rowIndex;

    private static final Type<SetFocusHandler> TYPE = new Type<SetFocusHandler>();


    protected SetFocus() {
        // Possibly for serialization.
    }

    @Override
    public Type<SetFocusHandler> getAssociatedType() {
        return TYPE;
    }

    public SetFocus(Long rowIndex) {
        this.rowIndex = rowIndex;
    }

    public static void fire(HasHandlers source, Long rowIndex) {
        SetFocus eventInstance = new SetFocus(rowIndex);
        source.fireEvent(eventInstance);
    }


    public static Type<SetFocusHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SetFocusHandler handler) {
        handler.onSetFocus(this);
    }

    public Long getRowIndex() {
        return rowIndex;
    }
}
