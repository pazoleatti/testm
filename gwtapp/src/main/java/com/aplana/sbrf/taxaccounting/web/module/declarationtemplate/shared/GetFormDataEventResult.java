package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.gwtplatform.dispatch.shared.Result;

import java.util.LinkedList;
import java.util.List;

public class GetFormDataEventResult implements Result {

    private List<FormDataEvent> eventList = new LinkedList<FormDataEvent>();

    public List<FormDataEvent> getEventList() {
        return eventList;
    }
}
