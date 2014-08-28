package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class FormTemplateMainEvent extends GwtEvent<FormTemplateMainEvent.MyHandler> {

    private TaxType taxType;

    public interface MyHandler extends EventHandler {
        void onSetData(FormTemplateMainEvent event);
    }

    private static final GwtEvent.Type<MyHandler> TYPE = new GwtEvent.Type<MyHandler>();

    public static GwtEvent.Type<MyHandler> getType() {
        return TYPE;
    }

    public FormTemplateMainEvent(TaxType taxType) {
        this.taxType = taxType;
    }

    public static void fire(HasHandlers source, TaxType taxType) {
        source.fireEvent(new FormTemplateMainEvent(taxType));
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onSetData(this);
    }

    @Override
    public GwtEvent.Type<MyHandler> getAssociatedType() {
        return getType();
    }

    public TaxType getTaxType() {
        return taxType;
    }
}
