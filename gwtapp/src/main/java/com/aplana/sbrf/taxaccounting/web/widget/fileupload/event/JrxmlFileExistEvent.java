package com.aplana.sbrf.taxaccounting.web.widget.fileupload.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 * Событие в случае, если есть декларации, которые используют jrxml этого макета
 */
public class JrxmlFileExistEvent extends GwtEvent<JrxmlFileExistEvent.JrxmlFileExistHandler> {
    private static final Type<JrxmlFileExistHandler> TYPE = new Type<JrxmlFileExistHandler>();

    private String uploadUuid, errorUuid;

    public static Type<JrxmlFileExistHandler> getType() {
        return TYPE;
    }

    /**
     *
     * @param uploadUuid uuid загруженного файла
     * @param errorUuid uuid ошибок
     */
    public static void fire(HasHandlers source, String uploadUuid, String errorUuid) {
        source.fireEvent(new JrxmlFileExistEvent(uploadUuid, errorUuid));
    }

    @Override
    public Type<JrxmlFileExistHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(JrxmlFileExistHandler handler) {
        handler.onJrxmlExist(new JrxmlFileExistEvent(uploadUuid, errorUuid));
    }

    public interface JrxmlFileExistHandler extends EventHandler {
        void onJrxmlExist(JrxmlFileExistEvent event);
    }

    public JrxmlFileExistEvent() {
    }

    public JrxmlFileExistEvent(String uploadUuid, String errorUuid) {
        this.uploadUuid = uploadUuid;
        this.errorUuid = errorUuid;
    }

    public String getUploadUuid() {
        return uploadUuid;
    }

    public String getErrorUuid() {
        return errorUuid;
    }
}
