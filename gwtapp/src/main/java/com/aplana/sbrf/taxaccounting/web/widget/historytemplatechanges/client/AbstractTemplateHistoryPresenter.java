package com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.client;

import com.aplana.sbrf.taxaccounting.model.VersionHistorySearchOrdering;
import com.aplana.sbrf.taxaccounting.web.main.api.client.AplanaUiHandlers;
import com.aplana.sbrf.taxaccounting.web.main.api.client.sortable.ViewWithSortableTable;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.SortFilter;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.shared.TemplateChangesExt;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;

import java.util.List;

/**
 * @author Fail Mukhametdinov
 */
public abstract class AbstractTemplateHistoryPresenter extends PresenterWidget<TemplateHistoryView>
        implements AplanaUiHandlers {

    protected DispatchAsync dispatchAsync;
    protected Integer id;

    public AbstractTemplateHistoryPresenter(EventBus eventBus, TemplateHistoryView view, DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    public void init(Integer declarationTemplateId) {
        this.id = declarationTemplateId;
        prepareHistory(declarationTemplateId, getFilter());
    }

    @Override
    public void onRangeChange(int start, int length) {
        prepareHistory(this.id, getFilter());
    }

    protected SortFilter getFilter() {
        SortFilter filter = new SortFilter();
        filter.setAscSorting(getView().isAscSorting());
        filter.setSearchOrdering(getView().getSearchOrdering());
        return filter;
    }

    protected abstract void prepareHistory(Integer id, SortFilter filter);

    public interface MyView extends PopupView, ViewWithSortableTable {
        void fillTemplate(List<TemplateChangesExt> templateChangesExts);

        VersionHistorySearchOrdering getSearchOrdering();
    }
}
