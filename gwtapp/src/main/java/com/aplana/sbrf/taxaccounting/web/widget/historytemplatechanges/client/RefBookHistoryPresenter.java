package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;

/**
 * Презентор истории изменений справочника
 *
 * @author lhaziev
 */
public class RefBookHistoryPresenter extends AbstractTemplateHistoryPresenter {

    @Inject
    public RefBookHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view, dispatcher);
    }

    @Override
    protected void prepareHistory(Integer id, SortFilter filter) {
        GetRefBookHistoryAction action = new GetRefBookHistoryAction();
        action.setTypeId(id);
        action.setSortFilter(filter);
        dispatchAsync.execute(action, CallbackUtils.defaultCallback(new AbstractCallback<GetRefBookHistoryResult>() {
            @Override
            public void onSuccess(GetRefBookHistoryResult result) {
                getView().fillTemplate(result.getChangesExtList());
            }
        }, this));
    }

    public interface MyView extends AbstractTemplateHistoryPresenter.MyView {
    }
}
