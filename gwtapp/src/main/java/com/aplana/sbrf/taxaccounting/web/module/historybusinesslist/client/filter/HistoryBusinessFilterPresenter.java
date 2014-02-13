package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.event.LogBusinessSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessFilterAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessFilterResult;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.GetHistoryBusinessReportPeriodsResult;
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

    public interface MyView extends View, HasUiHandlers<HistoryBusinessUIHandler> {

        void init();
        // Получение значений фильтра
        LogBusinessFilterValues getDataFilter();

        void setDepartments(List<Department> list, Set<Integer> availableValues);

        /*void setFormTypeId(List<Long> formTypeIds);*/

        void setDeclarationType(Map<Integer, String> declarationTypesMap);

        void setFormDataTaxType(List<TaxType> taxTypeList);

        void setReportPeriodPicker(List<ReportPeriod> reportPeriods);
    }

    public void initFilterData() {
        getView().init();
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

    public LogBusinessFilterValues getLogSystemFilter() {
        return getView().getDataFilter();
    }
}
