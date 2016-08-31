package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * Обновление информации о блокировках
 * User: lhaziev
 */
public class OnTimerEvent extends
        GwtEvent<OnTimerEvent.OnTimerHandler> {


    private static final Type<OnTimerHandler> TYPE = new Type<OnTimerHandler>();

    public static Type<OnTimerHandler> getType() {
        return TYPE;
    }

    @Override
    public Type<OnTimerHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(OnTimerHandler handler) {
        handler.onTimer(this);
    }

    public interface OnTimerHandler extends EventHandler {
        /**
         * @param event
         */
        void onTimer(OnTimerEvent event);
    }

    public static void fire(HasHandlers source) {
        source.fireEvent(new OnTimerEvent());
    }

}
