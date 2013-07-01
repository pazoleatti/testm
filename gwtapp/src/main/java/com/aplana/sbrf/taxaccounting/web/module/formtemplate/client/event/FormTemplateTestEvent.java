package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event;

import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class FormTemplateTestEvent extends GwtEvent<FormTemplateTestEvent.MyHandler> {

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public FormTemplateTestEvent(FormTemplate formTemplate) {
        this.formTemplate = formTemplate;
    }

    public static Type<MyHandler> getType(){
        return TYPE;
    }

    public static void fire(HasHandlers source, FormTemplate formTemplate) {
        source.fireEvent(new FormTemplateTestEvent(formTemplate));
    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onTest(this);
    }

    public interface MyHandler extends EventHandler{
        void onTest(FormTemplateTestEvent event);
    }

    private final FormTemplate formTemplate;

    public FormTemplate getFormTemplate() {
        return formTemplate;
    }
}
