package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

public class AddScriptEvent extends GwtEvent<AddScriptEvent.AddScriptHandler>{

    public interface  AddScriptHandler extends EventHandler {
        void onAdd(AddScriptEvent event);
    }

    private int formDataEventId;

    public int getFormDataEventId() {
        return formDataEventId;
    }

    public void setFormDataEventId(int formDataEventId) {
        this.formDataEventId = formDataEventId;
    }

    public AddScriptEvent(int formDataEventId) {
        this.formDataEventId = formDataEventId;
    }

    private static final GwtEvent.Type<AddScriptEvent.AddScriptHandler> TYPE = new GwtEvent.Type<AddScriptEvent.AddScriptHandler>();

    public static GwtEvent.Type<AddScriptEvent.AddScriptHandler> getType() {
        return TYPE;
    }
    @Override
    public Type<AddScriptEvent.AddScriptHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(AddScriptHandler handler) {
        handler.onAdd(this);
    }

    public static void fire(HasHandlers source, int formDataEventId) {
        source.fireEvent(new AddScriptEvent(formDataEventId));
    }
}
