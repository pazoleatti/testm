package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.shared.TemplateChangesExt;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetVersionHistoryResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * User: avanteev
 */
public class VersionHistoryPresenter extends PresenterWidget<VersionHistoryView> {

    public static enum TemplateType{
        DECLARATION,
        FORM
    }

    private final DispatchAsync dispatcher;

    public interface MyView extends PopupView {
        void fillTemplate(List<TemplateChangesExt> templateChangeses);
    }

    @Inject
    public VersionHistoryPresenter(EventBus eventBus, VersionHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
    }

    public void initHistory(int typeId, TemplateType templateType){
        GetVersionHistoryAction action = new GetVersionHistoryAction();
        action.setTypeId(typeId);
        action.setTemplateType(templateType);
        dispatcher.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetVersionHistoryResult>() {
            @Override
            public void onSuccess(GetVersionHistoryResult result) {
                getView().fillTemplate(result.getChangeses());
            }
        }, this));
    }

}
