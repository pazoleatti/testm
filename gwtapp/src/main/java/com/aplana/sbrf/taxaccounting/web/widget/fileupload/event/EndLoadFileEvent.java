package com.aplana.sbrf.taxaccounting.web.widget.fileupload.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class EndLoadFileEvent extends GwtEvent<EndLoadFileEvent.EndLoadFileHandler> {

    private static final Type<EndLoadFileHandler> TYPE = new Type<EndLoadFileHandler>();

    public static void fire(HasHandlers source, String uuid, Boolean isHasError) {
        source.fireEvent(new EndLoadFileEvent(uuid, isHasError));
    }

    public static Type<EndLoadFileHandler> getType() {
        return TYPE;
    }

    public interface EndLoadFileHandler extends EventHandler {
        void onEndLoad(EndLoadFileEvent event);
    }

    /**
     * Идентификатор логгера
     */
    private String uuid;
    /**
     * Если true - значит в {@link com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent#uuid}
     * находится список ошибок
     */
    private Boolean isHasError;

    public EndLoadFileEvent() {
    }

    public EndLoadFileEvent(String uuid) {
        this.uuid = uuid;
        this.isHasError = false;
    }

    public EndLoadFileEvent(Boolean isHasError) {
        this.isHasError = isHasError;
        this.uuid = null;
    }

    public EndLoadFileEvent(String uuid, Boolean isHasError) {
        this.uuid = uuid;
        this.isHasError = isHasError;
    }
    @Override
    public Type<EndLoadFileHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(EndLoadFileHandler handler) {
        handler.onEndLoad(new EndLoadFileEvent(uuid, isHasError));
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean isHasError() {
        return isHasError;
    }
}
