package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.event;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;

import java.util.List;

public class ChooseButtonEvent extends GwtEvent<ChooseButtonEvent.ChooseHandler> {
    private List<DataRow<Cell>> ndflReferences;

    public List<DataRow<Cell>> getNdflReferences() {
        return ndflReferences;
    }

    public ChooseButtonEvent(List<DataRow<Cell>> ndflReferences) {
        this.ndflReferences = ndflReferences;
    }

    private static final GwtEvent.Type<ChooseButtonEvent.ChooseHandler> TYPE = new GwtEvent.Type<ChooseButtonEvent.ChooseHandler>();

    public static GwtEvent.Type<ChooseButtonEvent.ChooseHandler> getType() {
        return TYPE;
    }

    @Override
    public GwtEvent.Type<ChooseButtonEvent.ChooseHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(ChooseButtonEvent.ChooseHandler handler) {
        handler.onChoice(this);
    }

    public interface ChooseHandler extends EventHandler {
        void onChoice(ChooseButtonEvent event);
    }

    public static void fire(HasHandlers source, List<DataRow<Cell>> ndflReferences) {
        source.fireEvent(new ChooseButtonEvent(ndflReferences));    }
}
