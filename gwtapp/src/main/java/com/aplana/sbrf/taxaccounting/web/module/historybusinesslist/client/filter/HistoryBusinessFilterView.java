package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookMultiPickerModalWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.*;

/**
 * User: avanteev
 */
public class HistoryBusinessFilterView extends ViewWithUiHandlers<HistoryBusinessUIHandler> implements HistoryBusinessFilterPresenter.MyView,
        Editor<LogBusinessFilterValues> {


    interface Binder extends UiBinder<Widget, HistoryBusinessFilterView> {
    }

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    DateMaskBoxPicker fromSearchDate;

    @UiField
    DateMaskBoxPicker toSearchDate;

    @Ignore
    @UiField
    DepartmentPickerPopupWidget departmentSelectionTree;

    @UiField
    RefBookMultiPickerModalWidget formTypeId;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeIds;

    @UiField
    RefBookMultiPickerModalWidget formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @Ignore
    @UiField(provided = true)
    ValueListBox<AuditFormType> auditFormTypeId;

    @UiField
    Panel formPanel;

    @UiField
    Panel declarationTypePanel;

    @UiField
    RefBookMultiPickerModalWidget user;

    private Map<Integer, String> declarationTypesMap;
    private static final int oneDayTime = 24 * 60 * 60 * 1000;

    @Override
    public LogBusinessFilterValues getDataFilter() {
        LogBusinessFilterValues lbf = new LogBusinessFilterValues();
        // Отчетные периоды
        lbf.setReportPeriodIds(reportPeriodIds.getValue());
        // Подразделение
        if (departmentSelectionTree.getValue() != null && !departmentSelectionTree.getValue().isEmpty()) {
            lbf.setDepartmentId(departmentSelectionTree.getValue().get(0));
        }
        // Тип формы
        lbf.setAuditFormTypeId(auditFormTypeId.getValue() == null ? null : auditFormTypeId.getValue().getId());
        // Вид налоговой формы
        lbf.setFormTypeId(formTypeId.getValue().isEmpty()? null : Integer.valueOf(String.valueOf(formTypeId.getValue().get(0))));
        // Вид декларации
        lbf.setDeclarationTypeId(declarationTypeIds.getValue());
        // Тип налоговой формы
        lbf.setFormKind(formDataKind.getValue());
        // Период
        lbf.setFromSearchDate(fromSearchDate.getValue());
        lbf.setToSearchDate(new Date(oneDayTime + toSearchDate.getValue().getTime()));
        // Пользователь
        lbf.setUserId(user.getSingleValue());
        lbf.setTaxType(taxType.getValue());
        return lbf;
    }

    @Override
    public void setDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentSelectionTree.setAvalibleValues(list, availableValues);
    }

 /*   @Override
    public void setFormTypeId(List<Long> formTypeIds) {
        formTypeId.setAcceptableValues(formTypesMap.keySet());

    }*/

    @Override
    public void setDeclarationType(Map<Integer, String> declarationTypesMap) {
        this.declarationTypesMap = declarationTypesMap;
        declarationTypeIds.setAcceptableValues(declarationTypesMap.keySet());
    }

    @Override
    public void setFormDataTaxType(List<TaxType> taxTypeList) {
        taxType.setAcceptableValues(taxTypeList);
    }

    @Override
    public void setReportPeriodPicker(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public void clearFilter() {
        formTypeId.setValue(null, false);
        formTypeId.setDereferenceValue(null);
        reportPeriodIds.setValue(null);
        taxType.setValue(null, true);
        departmentSelectionTree.setValue(null, true);
        declarationTypeIds.setValue(null);
        auditFormTypeId.setValue(null, true);
        user.setValue(null, false);
        user.setDereferenceValue(null);
        formDataKind.setValue(null, false);
        formDataKind.setDereferenceValue(null);
    }


    @Inject
    public HistoryBusinessFilterView(Binder binder) {
        auditFormTypeId = new ValueListBox<AuditFormType>(new AbstractRenderer<AuditFormType>() {
            @Override
            public String render(AuditFormType s) {
                if (s == null) {
                    return "";
                }
                return s.getName();
            }
        });

        auditFormTypeId.setValue(AuditFormType.FORM_TYPE_TAX);
        auditFormTypeId.setValue(AuditFormType.FORM_TYPE_DECLARATION);
        auditFormTypeId.setValue(null);

        taxType = new ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
            @Override
            public String render(TaxType object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        declarationTypeIds = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return declarationTypesMap.get(object);
            }
        });
        declarationTypeIds.setEnabled(false);

        initWidget(binder.createAndBindUi(this));

        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
        user.setEndDate(new Date());
        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        reportPeriodIds.setEnabled(false);
        formTypeId.setEnabled(false);
    }

    private void setVisibleTaxFields() {
        formPanel.setVisible(true);
        declarationTypePanel.setVisible(false);
        declarationTypeIds.setValue(null);
        List<Long> ids = new ArrayList<Long>();
        ids.add((long) FormDataKind.PRIMARY.getId());
        formDataKind.setValue(ids);
    }

    private void setVisibleDeclarationFields() {
        formPanel.setVisible(false);
        formTypeId.setValue(null, true);
        formDataKind.setValue(new ArrayList<Long>());
        declarationTypePanel.setVisible(true);
    }

    private void hideAll() {
        formPanel.setVisible(false);
        formTypeId.setValue(null, true);
        formDataKind.setValue(new ArrayList<Long>());
        declarationTypePanel.setVisible(false);
        declarationTypeIds.setValue(null);
    }

    @UiHandler("auditFormTypeId")
    public void onClick(ValueChangeEvent<AuditFormType> event) {
        if (event.getValue() == AuditFormType.FORM_TYPE_TAX) {
            setVisibleTaxFields();
        } else if (event.getValue() == AuditFormType.FORM_TYPE_DECLARATION) {
            setVisibleDeclarationFields();
        } else {
            hideAll();
        }
    }

    @UiHandler("search")
    void onAppyButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null) {
            getUiHandlers().onSearchClicked();
        }
    }

    @UiHandler("taxType")
    void onTaxTypeValueChange(ValueChangeEvent<TaxType> event) {
        if (taxType.getValue() == null){
            reportPeriodIds.setEnabled(false);
            reportPeriodIds.setValue(null, true);
            return;
        }
        if (getUiHandlers() != null) {
            getUiHandlers().getReportPeriods(event.getValue());
            reportPeriodIds.setEnabled(true);
        }
    }

    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event){

        if (event.getValue() != null && !event.getValue().isEmpty() && auditFormTypeId.getValue() != null){
            if (event.getValue() != null && !event.getValue().isEmpty()){
                Pair<Date, Date> datePair = reportPeriodIds.getPeriodDates(event.getValue().get(0));
                switch (auditFormTypeId.getValue()){
                    case FORM_TYPE_TAX:
                        formTypeId.setPeriodDates(datePair.getFirst(), datePair.getSecond());
                        formTypeId.setEnabled(true);
                        break;
                    case FORM_TYPE_DECLARATION:
                        declarationTypeIds.setValue(null);
                        declarationTypeIds.setEnabled(true);
                        break;
                }
            }
        } else {
            formTypeId.setValue(null, true);
            formTypeId.setEnabled(false);
            declarationTypeIds.setValue(null);
            declarationTypeIds.setEnabled(false);
        }
    }

    @UiHandler("auditFormTypeId")
    public void onAuditFormTypeChange(ValueChangeEvent<AuditFormType> event){
        if (event.getValue() == null){
            hideAll();
            return;
        }
        switch (event.getValue()){
            case FORM_TYPE_TAX:
                if (reportPeriodIds.getValue() == null || reportPeriodIds.getValue().isEmpty()){
                    setVisibleTaxFields();
                    return;
                }
                Pair<Date, Date> datePair = reportPeriodIds.getPeriodDates(reportPeriodIds.getValue().get(0));
                formTypeId.setPeriodDates(datePair.getFirst(), datePair.getSecond());
                formTypeId.setEnabled(true);
                setVisibleTaxFields();
                break;
            case FORM_TYPE_DECLARATION:
                if (reportPeriodIds.getValue() == null || reportPeriodIds.getValue().isEmpty()){
                    setVisibleDeclarationFields();
                    return;
                }
                declarationTypeIds.setEnabled(true);
                setVisibleDeclarationFields();
                break;
        }
    }
}
