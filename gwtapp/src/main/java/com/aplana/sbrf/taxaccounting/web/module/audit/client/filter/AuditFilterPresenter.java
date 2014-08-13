package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.*;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFilterPresenter extends PresenterWidget<AuditFilterPresenter.MyView> implements AuditFilterUIHandlers {

    private LogSystemAuditFilter logSystemAuditFilter;
    private LogSystemAuditFilter previousLogSystemAuditFilter;

    @Inject
    public AuditFilterPresenter(EventBus eventBus, MyView view) {
        super(eventBus, view);
        getView().setUiHandlers(this);
    }

    public interface MyView extends View, HasUiHandlers<AuditFilterUIHandlers> {
        void init();
        boolean isSearchResults();
        LogSystemAuditFilter getFilterData();
        boolean isChangeFilter();
        void edit(LogSystemAuditFilter auditFilter);
        void clear();
        void setSearchCriteria(String str);
    }

    public void initFilterData() {
        getView().init();
        previousLogSystemAuditFilter = getView().getFilterData();
    }

    @Override
    public void onSearchButtonClicked() {
        previousLogSystemAuditFilter = getView().getFilterData();
        if (!getView().isSearchResults()) {
            previousLogSystemAuditFilter.setOldLogSystemAuditFilter(null);
        } else {
            if (logSystemAuditFilter != null && logSystemAuditFilter.getFilter() != null &&
                    logSystemAuditFilter.getFilter().equals(previousLogSystemAuditFilter.getFilter()) &&
                    logSystemAuditFilter.getAuditFieldList().equals(previousLogSystemAuditFilter.getAuditFieldList()) &&
                    logSystemAuditFilter.getToSearchDate().equals(previousLogSystemAuditFilter.getToSearchDate()) &&
                    logSystemAuditFilter.getFromSearchDate().equals(previousLogSystemAuditFilter.getFromSearchDate())) {
                previousLogSystemAuditFilter = logSystemAuditFilter;
            } else {
                previousLogSystemAuditFilter.setOldLogSystemAuditFilter(logSystemAuditFilter);
            }
        }
        logSystemAuditFilter = new LogSystemAuditFilter(previousLogSystemAuditFilter);
        getView().edit(previousLogSystemAuditFilter);
        getView().setSearchCriteria(previousLogSystemAuditFilter.toString());
        AuditClientSearchEvent.fire(this);
    }

    public LogSystemAuditFilter getLogSystemFilter() {
        return isFilterChange() ? previousLogSystemAuditFilter : getView().getFilterData();
    }

    public boolean isFilterChange(){
        return getView().isChangeFilter();
    }

    @Override
    protected void onHide() {
        super.onHide();
        getView().clear();
    }
}
