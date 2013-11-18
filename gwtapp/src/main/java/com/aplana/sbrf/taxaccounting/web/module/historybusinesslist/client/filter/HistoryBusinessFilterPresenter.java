package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.event.LogBusinessPrintEvent;
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
        LogBusinessSearchEvent.fire(this);
    }

    @Override
    public void onPrintButtonClicked() {
        LogBusinessPrintEvent.fire(this);
    }

    public interface MyView extends View,HasUiHandlers<HistoryBusinessUIHandler> {
        // Получение значений фильтра
        LogBusinessFilterValues getDataFilter();

        void setDepartments(List<Department> list, Set<Integer> availableValues);
        void setFormTypeId(Map<Integer, String> formTypesMap);
        void setDeclarationType(Map<Integer, String> declarationTypesMap);
        void setFormDataKind(List<FormDataKind> list);
        void setFormDataTaxType(List<TaxType> taxTypeList);
        void setReportPeriodPicker(List<ReportPeriod> reportPeriods);
    }

    public void initFilterData(){

        GetHistoryBusinessFilterAction action = new GetHistoryBusinessFilterAction();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetHistoryBusinessFilterResult>() {

                    @Override
                    public void onSuccess(GetHistoryBusinessFilterResult result) {
                        LogSystemFilterAvailableValues avaliableValues = result.getAvailableValues();
                        getView().setDepartments(avaliableValues.getDepartments(), avaliableValues.getDepartmentIds());
                        getView().setFormTypeId(avaliableValues.getFormTypeMapIds());
                        getView().setDeclarationType(avaliableValues.getDeclarationMapIds());
                        getView().setFormDataKind(avaliableValues.getFormDataKinds());
                        getView().setFormDataTaxType(avaliableValues.getTaxTypes());
                    }
                }, this));

    }

    public LogBusinessFilterValues getLogSystemFilter(){
        return getView().getDataFilter();
    }
}
