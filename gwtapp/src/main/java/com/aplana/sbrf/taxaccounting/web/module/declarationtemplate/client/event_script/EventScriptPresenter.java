package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.AddScriptEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetFormDataEventAction;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.GetFormDataEventResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

public class EventScriptPresenter extends PresenterWidget<EventScriptPresenter.MyView> implements EventScriptUiHandlers{

    public interface MyView extends PopupView, HasUiHandlers<EventScriptUiHandlers> {
        void updateTableData(List<FormDataEvent> eventList);
    }

    private final DispatchAsync dispatcher;

    @Inject
    public EventScriptPresenter(EventBus eventBus, MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        dispatcher.execute(new GetFormDataEventAction(), CallbackUtils.defaultCallback(new AbstractCallback<GetFormDataEventResult>() {
            @Override
            public void onSuccess(GetFormDataEventResult result) {
                getView().updateTableData(result.getEventList());
            }
        }, EventScriptPresenter.this));
    }

    @Override
    public void onCreate(int formDataEventId) {
        AddScriptEvent.fire(this, formDataEventId);
        getView().hide();
    }

    @Override
    public void onClose() {
        getView().hide();
    }
}
