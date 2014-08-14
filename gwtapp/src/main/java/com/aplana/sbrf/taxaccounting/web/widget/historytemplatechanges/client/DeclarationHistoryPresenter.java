package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetDTHistoryResult;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.SortFilter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор истории изменений шаблона всех версий декларации.
 *
 * @author Fail Mukhametdinov
 */
public class DeclarationHistoryPresenter extends AbstractTemplateHistoryPresenter {

    @Inject
    public DeclarationHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view, dispatcher);
    }

    @Override
    protected void prepareHistory(Integer id, SortFilter filter) {
        GetDTHistoryAction action = new GetDTHistoryAction();
        action.setTypeId(id);
        action.setSortFilter(filter);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetDTHistoryResult>() {
            @Override
            public void onSuccess(GetDTHistoryResult result) {
                getView().fillTemplate(result.getChangesExtList());
            }
        }, this));
    }

    public interface MyView extends AbstractTemplateHistoryPresenter.MyView {
    }
}
