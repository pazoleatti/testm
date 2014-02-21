package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.LogSystemFilterAvailableValues;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.event.LogBusinessSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.*;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: avanteev
 */
public class HistoryBusinessFilterPresenter extends PresenterWidget<HistoryBusinessFilterPresenter.MyView> implements HistoryBusinessUIHandler {

    private DispatchAsync dispatchAsync;
    private LogSystemAuditFilter previousLogSystemAuditFilter;

    @Inject
    public HistoryBusinessFilterPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void getReportPeriods(TaxType taxType) {
        GetHistoryBusinessReportPeriodsAction action = new GetHistoryBusinessReportPeriodsAction();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, new AbstractCallback<GetHistoryBusinessReportPeriodsResult>() {
            @Override
            public void onSuccess(GetHistoryBusinessReportPeriodsResult result) {
                getView().setReportPeriodPicker(result.getReportPeriodList());
            }
        });
    }

    @Override
    public void onSearchClicked() {
        previousLogSystemAuditFilter = getView().getDataFilter();
        getView().edit(previousLogSystemAuditFilter);
        LogBusinessSearchEvent.fire(this);
    }

    public interface MyView extends View, HasUiHandlers<HistoryBusinessUIHandler> {

        void init();
        // Получение значений фильтра
        LogSystemAuditFilter getDataFilter();

        void setDepartments(List<Department> list, Set<Integer> availableValues);

        /*void setFormTypeId(List<Long> formTypeIds);*/

        void setDeclarationType(Map<Integer, String> declarationTypesMap);

        void setFormDataTaxType(List<TaxType> taxTypeList);

        void setReportPeriodPicker(List<ReportPeriod> reportPeriods);
        boolean isChangeFilter();
        void edit(LogSystemAuditFilter auditFilter);
    }

    public void initFilterData() {
        getView().init();
        previousLogSystemAuditFilter = getView().getDataFilter();
        GetHistoryBusinessFilterAction action = new GetHistoryBusinessFilterAction();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetHistoryBusinessFilterResult>() {

                    @Override
                    public void onSuccess(GetHistoryBusinessFilterResult result) {
                        LogSystemFilterAvailableValues avaliableValues = result.getAvailableValues();
                        getView().setDepartments(avaliableValues.getDepartments(), avaliableValues.getDepartmentIds());
                        /*getView().setFormTypeId(Lists.transform(avaliableValues.getFormTypeIds(), new Function<Integer, Long>() {
                            @Override
                            public Long apply(@Nullable Integer integer) {
                                if (integer == null)
                                    return null;
                                return Long.valueOf(integer);
                            }
                        }));*/
                        getView().setDeclarationType(avaliableValues.getDeclarationMapIds());
                        getView().setFormDataTaxType(avaliableValues.getTaxTypes());
                    }
                }, this));

    }

    public LogSystemAuditFilter getLogSystemFilter() {
        return isFilterChange() ? previousLogSystemAuditFilter : getView().getDataFilter();
    }

    public boolean isFilterChange(){
        return getView().isChangeFilter();
    }
}
