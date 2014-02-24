package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
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

    private final DispatchAsync dispatchAsync;

    private LogSystemAuditFilter previousLogSystemAuditFilter;

    @Inject
    public AuditFilterPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void getReportPeriods(TaxType taxType) {
        if (taxType == null) {
            getView().updateReportPeriodPicker(new ArrayList<ReportPeriod>());
            return;
        }
        GetReportPeriodsAction action = new GetReportPeriodsAction();
        action.setTaxType(taxType);
        dispatchAsync.execute(action, new AbstractCallback<GetReportPeriodsResult>() {
            @Override
            public void onSuccess(GetReportPeriodsResult result) {
                getView().updateReportPeriodPicker(result.getReportPeriods());
            }
        });
    }

    public interface MyView extends View, HasUiHandlers<AuditFilterUIHandlers> {
        void init();
        void setDepartments(List<Department> list, Set<Integer> availableValues);
        void setDeclarationType(Map<Integer, String> declarationTypesMap);
        void setFormDataTaxType(List<TaxType> taxTypeList);
        void updateReportPeriodPicker(List<ReportPeriod> reportPeriods);
        LogSystemAuditFilter getFilterData();
        boolean isChangeFilter();
        void edit(LogSystemAuditFilter auditFilter);
        void clear();
    }

    public void initFilterData() {
        GetAuditFilterDataAction action = new GetAuditFilterDataAction();
        getView().init();
        previousLogSystemAuditFilter = getView().getFilterData();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetAuditFilterDataResult>() {

                    @Override
                    public void onSuccess(GetAuditFilterDataResult result) {
                        LogSystemFilterAvailableValues auditFilterDataAvaliableValues = result.getAvailableValues();
                        getView().setDepartments(auditFilterDataAvaliableValues.getDepartments(),
                                convertDepartmentsToIds(auditFilterDataAvaliableValues.getDepartments()));
                        /*getView().setFormTypeId(Lists.transform(auditFilterDataAvaliableValues.getFormTypeIds(), new com.google.common.base.Function<Integer, Long>() {
                            @Override
                            public Long apply(@Nullable Integer integer) {
                                System.out.println("Integer ids: " + integer);
                                if (integer == null)
                                    return null;
                                return Long.valueOf(integer);
                            }
                        }));*/
                        getView().setDeclarationType(fillDeclarationTypeMap(auditFilterDataAvaliableValues.getDeclarationTypes()));
                        /*getView().setFormDataKind(result.getFormDataKinds());*/
                        getView().setFormDataTaxType(result.getTaxTypes());
                    }
                }, this));

    }

    @Override
    public void onSearchButtonClicked() {
        previousLogSystemAuditFilter = getView().getFilterData();
        getView().edit(previousLogSystemAuditFilter);
        AuditClientSearchEvent.fire(this);
    }

    private Set<Integer> convertDepartmentsToIds(List<Department> source) {
        Set<Integer> result = new HashSet<Integer>();
        for (Department department : source) {
            result.add(department.getId());
        }
        return result;
    }

    private Map<Integer, String> fillDeclarationTypeMap(List<DeclarationType> source) {
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for (DeclarationType formType : source) {
            formTypesMap.put(formType.getId(), formType.getName());
        }
        return formTypesMap;
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
