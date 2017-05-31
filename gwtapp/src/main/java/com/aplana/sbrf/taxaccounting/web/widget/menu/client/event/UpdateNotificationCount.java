package com.aplana.sbrf.taxaccounting.web.widget.menu.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Created by auldanov on 01.04.2014.
 */
public class UpdateNotificationCount extends GwtEvent<UpdateNotificationCount.UpdateNotificationCountHandler> {

    public interface UpdateNotificationCountHandler extends EventHandler {
        void updateNotificationCountHandler(UpdateNotificationCount event);
    }

    private static final Type<UpdateNotificationCountHandler> TYPE = new Type<UpdateNotificationCountHandler>();

    @Override
    public Type<UpdateNotificationCountHandler> getAssociatedType() {
        return TYPE;
    }

    public UpdateNotificationCount() {
    }

    public static void fire(HasHandlers source) {
        UpdateNotificationCount eventInstance = new UpdateNotificationCount();
        source.fireEvent(eventInstance);
    }


    public static Type<UpdateNotificationCountHandler> getType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UpdateNotificationCountHandler handler) {
        handler.updateNotificationCountHandler(this);
    }
}
