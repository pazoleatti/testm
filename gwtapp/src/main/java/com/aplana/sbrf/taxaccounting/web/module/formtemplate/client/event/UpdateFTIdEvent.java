package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class UpdateFTIdEvent extends GwtEvent<UpdateFTIdEvent.MyHandler> {
    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    private int ftId;

    public UpdateFTIdEvent(int ftId) {
        this.ftId = ftId;
    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return  getType();
    }

    public static Type<MyHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onUpdateId(this);
    }

    public static void fire(HasHandlers source, int ftId) {
        UpdateFTIdEvent event = new UpdateFTIdEvent(ftId);
        source.fireEvent(event);
    }

    public int getFtId() {
        return ftId;
    }

    public interface MyHandler extends EventHandler {
        /**
         * @param event
         */
        void onUpdateId(UpdateFTIdEvent event);
    }
}
