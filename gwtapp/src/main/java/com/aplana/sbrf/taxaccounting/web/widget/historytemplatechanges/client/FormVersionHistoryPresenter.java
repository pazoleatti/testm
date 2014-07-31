package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTVersionHistoryAction;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.GetFTVersionHistoryResult;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.SortFilter;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор истории изменений конкретной версии шаблона налоговой формы.
 *
 * @author Fail Mukhametdinov
 */
public class FormVersionHistoryPresenter extends AbstractTemplateHistoryPresenter {

    @Inject
    public FormVersionHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view, dispatcher);
    }

    @Override
    protected void prepareHistory(Integer id, SortFilter filter) {
        GetFTVersionHistoryAction action = new GetFTVersionHistoryAction();
        action.setTemplateId(id);
        action.setSortFilter(filter);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetFTVersionHistoryResult>() {
            @Override
            public void onSuccess(GetFTVersionHistoryResult result) {
                getView().fillTemplate(result.getChangesExtList());
            }
        }, this));
    }

    public interface MyView extends AbstractTemplateHistoryPresenter.MyView {
    }
}
