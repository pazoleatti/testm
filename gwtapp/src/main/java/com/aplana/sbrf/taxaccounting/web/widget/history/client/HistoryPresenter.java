package com.aplana.sbrf.taxaccounting.web.widget.history.client;

import com.aplana.sbrf.taxaccounting.model.HistoryBusinessSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.widget.history.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

public class HistoryPresenter extends PresenterWidget<HistoryView> implements AplanaUiHandlers {

    private final DispatchAsync dispatcher;
    private boolean isFormData;
    private long id;
    private TaxType taxType;

    @Inject
    public HistoryPresenter(final EventBus eventBus, final HistoryView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    @Override
    public void onRangeChange(int start, int length) {
        if (isFormData) {
            prepareFormHistory(id, taxType);
        } else {
            prepareDeclarationHistory(id, taxType);
        }
    }

    public void prepareDeclarationHistory(long declarationId, TaxType taxType) {
        this.isFormData = false;
        this.id = declarationId;
        this.taxType = taxType;
        getView().updateTitle(taxType);
        GetDeclarationLogsBusinessAction action = new GetDeclarationLogsBusinessAction();
        action.setId(declarationId);
        action.setFilter(getSortFilter());
        prepareHistory(action);
    }

    public void prepareFormHistory(long formId, TaxType taxType) {
        this.isFormData = true;
        this.id = formId;
        this.taxType = taxType;
        getView().updateTitle(taxType);
        GetFormLogsBusinessAction action = new GetFormLogsBusinessAction();
        action.setId(formId);
        action.setFilter(getSortFilter());
        prepareHistory(action);
    }

    private void prepareHistory(UnsecuredActionImpl<GetLogsBusinessResult> action) {
        dispatcher.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetLogsBusinessResult>() {
                    @Override
                    public void onSuccess(GetLogsBusinessResult result) {
                        getView().setHistory(result.getLogs());
                    }
                }, this));
    }

    public interface MyView extends PopupView, ViewWithSortableTable {
        void setHistory(List<LogBusinessClient> logs);

        void updateTitle(TaxType taxType1);

        HistoryBusinessSearchOrdering getSearchOrdering();
    }

    private SortFilter getSortFilter() {
        SortFilter filter = new SortFilter();
        filter.setSearchOrdering(getView().getSearchOrdering());
        filter.setAscSorting(getView().isAscSorting());
        return filter;
    }
}
