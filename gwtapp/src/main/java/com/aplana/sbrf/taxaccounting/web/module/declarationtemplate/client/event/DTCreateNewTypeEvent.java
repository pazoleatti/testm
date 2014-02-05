package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

/**
 * User: avanteev
 */
public class DTCreateNewTypeEvent extends GwtEvent<DTCreateNewTypeEvent.MyHandler> {
    public static interface MyHandler extends EventHandler {
        /**
         * @param event
         */
        void onCreateDTType(DTCreateNewTypeEvent event);
    }

    private TaxType taxType;

    public TaxType getTaxType() {
        return taxType;
    }

    private static final Type<MyHandler> TYPE = new Type<MyHandler>();

    public static Type<MyHandler> getType() {
        return TYPE;
    }

    public static void fire(HasHandlers source, TaxType taxType) {
        DTCreateNewTypeEvent event = new DTCreateNewTypeEvent(taxType);
        source.fireEvent(event);
    }

    public DTCreateNewTypeEvent(TaxType taxType) {
        this.taxType = taxType;
    }

    @Override
    protected void dispatch(MyHandler handler) {
        handler.onCreateDTType(this);

    }

    @Override
    public Type<MyHandler> getAssociatedType() {
        return getType();
    }

}
