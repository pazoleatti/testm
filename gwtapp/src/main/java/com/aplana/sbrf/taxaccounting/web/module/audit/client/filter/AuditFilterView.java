package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.newdepartmentpicker.NewDepartmentPicker;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodDataProvider;
import com.aplana.sbrf.taxaccounting.web.widget.reportperiodpicker.ReportPeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFilterView extends ViewWithUiHandlers<AuditFilterUIHandlers>
        implements AuditFilterPresenter.MyView, ReportPeriodDataProvider {


    private ReportPeriodPicker currentReportPeriod;

    interface Binder extends UiBinder<Widget, AuditFilterView> { }

    @UiField
    CustomDateBox fromSearchDate;

    @UiField
    CustomDateBox toSearchDate;

    @UiField
    VerticalPanel reportPeriodPanel;

    @UiField(provided = true)
    ValueListBox<Integer> userId;

    @UiField
    NewDepartmentPicker departmentSelectionTree;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeId;

    @UiField(provided = true)
    ValueListBox<FormDataKind> formKind;

    @UiField(provided = true)
    ValueListBox<TaxType> formDataTaxType;

    private Map<Integer, String> formTypesMap;
    private Map<Integer, String> userLoginMap;
    private Map<Integer, String> declarationTypesMap;

    @Override
    public void setDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentSelectionTree.setTreeValues(list,availableValues);
    }

    @Override
    public void setFormTypeId(Map<Integer, String> formTypesMap) {
        this.formTypesMap = formTypesMap;
        formTypeId.setAcceptableValues(formTypesMap.keySet());

    }

    @Override
    public void setDeclarationType(Map<Integer, String> declarationTypesMap) {
        this.declarationTypesMap = declarationTypesMap;
        declarationTypeId.setAcceptableValues(declarationTypesMap.keySet());
    }

    @Override
    public void setFormDataKind(List<FormDataKind> list) {
        formKind.setAcceptableValues(list);
    }

    @Override
    public void setFormDataTaxType(List<TaxType> taxTypeList) {
        formDataTaxType.setAcceptableValues(taxTypeList);
    }

    @Override
    public void updateTaxPeriodPicker(List<TaxPeriod> taxPeriods) {
        if(currentReportPeriod != null){
            reportPeriodPanel.remove(currentReportPeriod);
        }
        currentReportPeriod = new ReportPeriodPicker(this);
        currentReportPeriod.setTaxPeriods(taxPeriods == null?new ArrayList<TaxPeriod>():taxPeriods);
        reportPeriodPanel.add(currentReportPeriod);
    }

    @Override
    public void updateReportPeriodPicker(List<ReportPeriod> reportPeriods) {
        currentReportPeriod.setReportPeriods(reportPeriods);
    }

    @Override
    public LogSystemFilter getFilterData() {
        LogSystemFilter lsf = new LogSystemFilter();
        List<Integer> reportPeriods = new ArrayList<Integer>();

        //Antil we choose tax type
        if(currentReportPeriod !=null){
            for (Map.Entry<Integer, String> reportPeriod : currentReportPeriod.getSelectedReportPeriods().entrySet()){
                reportPeriods.add(reportPeriod.getKey());
            }
            lsf.setReportPeriodIds(reportPeriods);
        }

        List<Integer> departments = new ArrayList<Integer>();
        for (Map.Entry<String, Integer> department : departmentSelectionTree.getSelectedItems().entrySet()) {
            departments.add(department.getValue());
        }
        lsf.setDepartmentIds(departments);
        lsf.setFormTypeId((null == formTypeId.getValue()) ? 0 : formTypeId.getValue());
        lsf.setDeclarationTypeId((null == declarationTypeId.getValue()) ? 0 : declarationTypeId.getValue());
        lsf.setFormKind(formKind.getValue());
        lsf.setFromSearchDate(fromSearchDate.getValue());
        lsf.setToSearchDate(toSearchDate.getValue());
        lsf.setUserId(null == userId.getValue()? 0 : userId.getValue());
        return lsf;
    }

    @Override
    public void setDataFilter(LogSystemFilter dataFilter) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setUserLogins(Map<Integer, String> userLoginsMap) {
        this.userLoginMap = userLoginsMap;
        userId.setAcceptableValues(userLoginsMap.keySet());
    }

    @Override
    public void setValueListBoxHandler(ValueChangeHandler<TaxType> handler) {
        formDataTaxType.addValueChangeHandler(handler);
    }

    @Override
    public void setFromSearchDate(Date fromSearchDate) {
        this.fromSearchDate.setValue(fromSearchDate);
    }

    @Override
    public void setToSearchDate(Date toSearchDate) {
        this.toSearchDate.setValue(toSearchDate);
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

        formKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
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

        declarationTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return declarationTypesMap.get(object);
            }
        });

        userId = new ValueListBox<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return userLoginMap.get(object);
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
        currentReportPeriod = new ReportPeriodPicker(this);
        reportPeriodPanel.add(currentReportPeriod);
    }

    @UiHandler("search")
    void onSearchButtonClicked(ClickEvent event){
        if(getUiHandlers() != null)
            getUiHandlers().onSearchButtonClicked();
    }

}
