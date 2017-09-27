package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event_script;

import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.AddScriptEvent;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

public class EventScriptPresenter extends PresenterWidget<EventScriptPresenter.MyView> implements EventScriptUiHandlers{

    public interface MyView extends PopupView, HasUiHandlers<EventScriptUiHandlers> {

    }

    @Inject
    public EventScriptPresenter(EventBus eventBus, MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

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
