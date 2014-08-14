package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTHistoryResult;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.SortFilter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор истории изменений шаблона всех версий налоговой формы.
 *
 * @author Fail Mukhametdinov
 */
public class FormHistoryPresenter extends AbstractTemplateHistoryPresenter {

    @Inject
    public FormHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view, dispatcher);
    }

    @Override
    protected void prepareHistory(Integer id, SortFilter filter) {
        GetFTHistoryAction action = new GetFTHistoryAction();
        action.setTypeId(id);
        action.setSortFilter(filter);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetFTHistoryResult>() {
            @Override
            public void onSuccess(GetFTHistoryResult result) {
                getView().fillTemplate(result.getChangesExtList());
            }
        }, this));
    }

    public interface MyView extends AbstractTemplateHistoryPresenter.MyView {
    }
}
