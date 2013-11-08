package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientArchiveEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.client.event.AuditClientSearchEvent;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetAuditFilterDataResult;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetReportPeriodsAction;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.GetReportPeriodsResult;
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
    public void onSearchButtonClicked() {
        AuditClientSearchEvent.fire(this);
    }

    @Override
    public void onPrintButtonClicked() {
        AuditFilterPrintEvent.fire(this);
    }

    @Override
    public void onArchiveButtonClicked() {
        AuditClientArchiveEvent.fire(this);
    }


    public interface MyView extends View, HasUiHandlers<AuditFilterUIHandlers>{
        void setDepartments(List<Department> list, Set<Integer> availableValues);
        void setFormTypeId(Map<Integer, String> formTypesMap);
        void setDeclarationType(Map<Integer, String> declarationTypesMap);
        void setFormDataKind(List<FormDataKind> list);
        void setFormDataTaxType(List<TaxType> taxTypeList);
        void setValueListBoxHandler(ValueChangeHandler<TaxType> handler);
        void setFormTypeHandler(ValueChangeHandler<AuditFormType> handler);
        void updateReportPeriodPicker(List<ReportPeriod> reportPeriods);
        LogSystemFilter getFilterData();
        void setVisibleTaxFields();
        void setVisibleDeclarationFields();
        void hideAll();
    }

    public void initFilterData(){

        GetAuditFilterDataAction action = new GetAuditFilterDataAction();
        dispatchAsync.execute(action, CallbackUtils
                .defaultCallback(new AbstractCallback<GetAuditFilterDataResult>() {

            @Override
            public void onSuccess(GetAuditFilterDataResult result) {
                LogSystemFilterAvailableValues auditFilterDataAvaliableValues = result.getAvailableValues();
                getView().setDepartments(auditFilterDataAvaliableValues.getDepartments(),
                        convertDepartmentsToIds(auditFilterDataAvaliableValues.getDepartments()));
                getView().setFormTypeId(fillFormTypesMap(auditFilterDataAvaliableValues.getFormTypes()));
                getView().setDeclarationType(fillDeclarationTypeMap(auditFilterDataAvaliableValues.getDeclarationTypes()));
                getView().setFormDataKind(result.getFormDataKinds());
                getView().setFormDataTaxType(result.getTaxTypes());
            }
        }, this));

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
        };
        getView().setValueListBoxHandler(taxTypeValueChangeHandler);

        ValueChangeHandler<AuditFormType> formTypeValueChangeHandler = new ValueChangeHandler<AuditFormType>() {
            @Override
            public void onValueChange(ValueChangeEvent<AuditFormType> event) {
                if (event.getValue() == AuditFormType.FORM_TYPE_TAX) {
                    getView().setVisibleTaxFields();
                } else if (event.getValue() == AuditFormType.FORM_TYPE_DECLARATION) {
                    getView().setVisibleDeclarationFields();
                } else {
                    getView().hideAll();
                }
            }
        };
        getView().setFormTypeHandler(formTypeValueChangeHandler);
    }
}
