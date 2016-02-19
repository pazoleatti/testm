package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class UpdateTemplateEvent extends GwtEvent<UpdateTemplateEvent.MyHandler> {
    public interface MyHandler extends EventHandler {
        /**
         * @param event
         */
        void onUpdateTemplate(UpdateTemplateEvent event);
    }

    public UpdateTemplateEvent() {
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static Type<MyHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source) {
        UpdateTemplateEvent event = new UpdateTemplateEvent();
        source.fireEvent(event);
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onUpdateTemplate(this);
    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }
}
