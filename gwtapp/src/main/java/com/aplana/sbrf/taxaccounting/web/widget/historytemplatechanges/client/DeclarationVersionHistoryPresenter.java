package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTVersionHistoryResult;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.SortFilter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор истории изменений конкретной версии шаблона декларации.
 *
 * @author Fail Mukhametdinov
 */
public class DeclarationVersionHistoryPresenter extends AbstractTemplateHistoryPresenter {

    @Inject
    public DeclarationVersionHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view, dispatcher);
    }

    @Override
    protected void prepareHistory(Integer id, SortFilter filter) {
        GetDTVersionHistoryAction action = new GetDTVersionHistoryAction();
        action.setTemplateId(id);
        action.setSortFilter(filter);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDTVersionHistoryResult>() {
            @Override
            public void onSuccess(GetDTVersionHistoryResult result) {
                getView().fillTemplate(result.getChangesExtList());
            }
        }, this));
    }

    public interface MyView extends AbstractTemplateHistoryPresenter.MyView {
    }
}
