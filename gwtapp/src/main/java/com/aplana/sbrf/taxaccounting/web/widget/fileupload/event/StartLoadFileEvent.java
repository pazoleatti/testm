package com.aplana.sbrf.taxaccounting.web.widget.fileupload.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class StartLoadFileEvent extends GwtEvent<StartLoadFileEvent.StartLoadFileHandler> {

    private static final Type<StartLoadFileHandler> TYPE = new Type<StartLoadFileHandler>();

    public static void fire(HasHandlers source, String filename) {
        source.fireEvent(new StartLoadFileEvent(filename));
    }

    public static Type<StartLoadFileHandler> getType() {
        return TYPE;
    }

    public interface StartLoadFileHandler extends EventHandler {
        void onStartLoad(StartLoadFileEvent event);
    }

    private String filename;

    public StartLoadFileEvent() {
    }

    public StartLoadFileEvent(String filename) {
        this.filename = filename;
    }

    @Override
    public Type<StartLoadFileHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(StartLoadFileHandler handler) {
        handler.onStartLoad(new StartLoadFileEvent(filename));
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
