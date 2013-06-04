package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFilterView extends ViewWithUiHandlers<AuditFilterUIHandlers> implements AuditFilterPresenter.MyView, ReportPeriodDataProvider {


    private ReportPeriodPicker currentReportPeriod;

    interface Binder extends UiBinder<Widget, AuditFilterView> { }

    @UiField
    CustomDateBox fromDateBox;

    @UiField
    CustomDateBox toDateBox;

    @UiField
    VerticalPanel reportPeriodPanel;

    @UiField(provided = true)
    ValueListBox<Integer> userLogin;

    @UiField
    NewDepartmentPicker departmentSelectionTree;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> formDataTaxType;

    private Map<Integer, String> formTypesMap;
    private Map<Integer, String> userLoginMap;

    @Override
    public void setDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentSelectionTree.setTreeValues(list,availableValues);
    }

    @Override
    public void setFormTypeId(Map<Integer, String> formTypesMap) {
        this.formTypesMap = formTypesMap;
        formTypeId.setValue(null);
        formTypeId.setAcceptableValues(formTypesMap.keySet());

    }

    @Override
    public void setFormDataKind(List<FormDataKind> list) {
        formDataKind.setAcceptableValues(list);
    }

    @Override
    public void setFormDataTaxType(List<TaxType> taxTypeList) {
        formDataTaxType.setAcceptableValues(taxTypeList);
    }

    @Override
    public void updateReportPeriodPicker(TaxType taxType, List<TaxPeriod> taxPeriods) {
        if(currentReportPeriod != null){
            reportPeriodPanel.remove(currentReportPeriod);
        }
        currentReportPeriod = new ReportPeriodPicker(this);
        currentReportPeriod.setTaxPeriods(taxPeriods);
        reportPeriodPanel.add(currentReportPeriod);
    }

    @Override
    public void updateReportPeriodPicker(List<ReportPeriod> reportPeriods) {
        currentReportPeriod.setReportPeriods(reportPeriods);
    }

    public void setUserLogins(Map<Integer, String> userLoginsMap) {
        this.userLoginMap = userLoginsMap;
        userLogin.setValue(null);
        userLogin.setAcceptableValues(userLoginsMap.keySet());
    }

    @Override
    public void setValueListBoxHandler(ValueChangeHandler<TaxType> handler) {
        formDataTaxType.addValueChangeHandler(handler);
    }

    @Override
    public void onTaxPeriodSelected(TaxPeriod taxPeriod) {
        if(getUiHandlers()!=null) {
            getUiHandlers().onTaxPeriodSelected(taxPeriod);
        }
    }

    @Inject
    @UiConstructor
    public AuditFilterView(final Binder uiBinder) {

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formDataTaxType = new ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
            @Override
            public String render(TaxType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

        userLogin = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return userLoginMap.get(object);
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
    }

}
