package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditFormSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetTaxPeriodResult;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriods;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared.GetReportPeriodsResult;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

    @Inject
    public AuditFilterPresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
        super(eventBus, view);
        this.dispatchAsync = dispatchAsync;
        getView().setUiHandlers(this);
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        GetReportPeriods action = new GetReportPeriods();
        action.setTaxPeriod(taxPeriod);
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetReportPeriodsResult>() {
                    @Override
                    public void onSuccess(GetReportPeriodsResult result) {
                        getView().updateReportPeriodPicker(result.getReportPeriods());
                    }
                }, this));
    }

    @Override
    public void onSearchButtonClicked() {
        AuditFormSearchEvent.fire(this);
    }


    public interface MyView extends View, HasUiHandlers<AuditFilterUIHandlers>{
        void setDepartments(List<Department> list, Set<Integer> availableValues);
        void setFormTypeId(Map<Integer, String> formTypesMap);
        void setDeclarationType(Map<Integer, String> declarationTypesMap);
        void setFormDataKind(List<FormDataKind> list);
        void setFormDataTaxType(List<TaxType> taxTypeList);
        void setUserLogins(Map<Integer, String> userLoginsMap);
        void setValueListBoxHandler(ValueChangeHandler<TaxType> handler);
        void setFromSearchDate(Date fromSearchDate);
        void setToSearchDate(Date toSearchDate);
        void updateTaxPeriodPicker(List<TaxPeriod> taxPeriods);
        void updateReportPeriodPicker(List<ReportPeriod> reportPeriods);
        LogSystemFilter getFilterData();
        void setDataFilter(LogSystemFilter dataFilter);
    }

    public void initFilterData(){

        GetAuditFilterDataAction action = new GetAuditFilterDataAction();
        dispatchAsync.execute(action, new AbstractCallback<GetAuditFilterDataResult>() {

            @Override
            public void onSuccess(GetAuditFilterDataResult result) {
                LogSystemFilterAvailableValues auditFilterDataAvaliableValues = result.getAvailableValues();
                getView().setDepartments(auditFilterDataAvaliableValues.getDepartments(),
                        convertDepartmentsToIds(auditFilterDataAvaliableValues.getDepartments()));
                getView().setFormTypeId(fillFormTypesMap(auditFilterDataAvaliableValues.getFormTypes()));
                getView().setDeclarationType(fillDeclarationTypeMap(auditFilterDataAvaliableValues.getDeclarationTypes()));
                getView().setFormDataKind(result.getFormDataKinds());
                getView().setFormDataTaxType(result.getTaxTypes());
                getView().setUserLogins(fillUserMap(auditFilterDataAvaliableValues.getUsers()));
                //getView().setDataFilter(prepareLogSystemFilter(result));
            }
        });

    }

    private LogSystemFilter prepareLogSystemFilter(GetAuditFilterDataResult result){
        LogSystemFilter logSystemFilter = new LogSystemFilter();
        logSystemFilter.setFromSearchDate(new Date());
        logSystemFilter.setToSearchDate(new Date());
        return logSystemFilter;
    }

    private Set<Integer> convertDepartmentsToIds(List<Department> source){
        Set<Integer> result = new HashSet<Integer>();
        for(Department department : source){
            result.add(department.getId());
        }
        return result;
    }

    private Map<Integer, String> fillFormTypesMap(List<FormType> source){
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for(FormType formType : source){
            formTypesMap.put(formType.getId(), formType.getName());
        }
        return formTypesMap;
    }

    private Map<Integer, String> fillDeclarationTypeMap(List<DeclarationType> source){
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for(DeclarationType formType : source){
            formTypesMap.put(formType.getId(), formType.getName());
        }
        return formTypesMap;
    }

    private Map<Integer, String> fillUserMap(List<TAUser> source){
        Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();
        formTypesMap.put(null, "");
        for(TAUser user : source){
            formTypesMap.put(user.getId(), user.getName());
        }
        return formTypesMap;
    }

    public LogSystemFilter getLogSystemFilter(){
        return getView().getFilterData();
    }

    @Override
    protected void onBind() {
        super.onBind();
        final ValueChangeHandler<TaxType> taxTypeValueChangeHandler = new ValueChangeHandler<TaxType>() {
            @Override
            public void onValueChange(ValueChangeEvent<TaxType> event) {
                final TaxType taxType = event.getValue();
                if(taxType == null) {
                    getView().updateTaxPeriodPicker(null);
                    return;
                }
                GetTaxPeriodAction action = new GetTaxPeriodAction();
                action.setTaxType(taxType);
                dispatchAsync.execute(action, new AbstractCallback<GetTaxPeriodResult>() {
                    @Override
                    public void onSuccess(GetTaxPeriodResult result) {
                        getView().updateTaxPeriodPicker(result.getTaxPeriods());
                    }
                });

            }
        };
        getView().setValueListBoxHandler(taxTypeValueChangeHandler);
    }
}
