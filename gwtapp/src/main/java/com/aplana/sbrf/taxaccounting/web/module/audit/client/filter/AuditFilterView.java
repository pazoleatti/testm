package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookMultiPickerModalWidget;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: avanteev
 * Date: 2013
 */
public class AuditFilterView extends ViewWithUiHandlers<AuditFilterUIHandlers>
        implements AuditFilterPresenter.MyView {

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    interface Binder extends UiBinder<Widget, AuditFilterView> {
    }

    @UiField
    DateMaskBoxPicker fromSearchDate;

    @UiField
    DateMaskBoxPicker toSearchDate;

    @UiField
    DepartmentPickerPopupWidget departmentSelectionTree;

    @UiField
    RefBookMultiPickerModalWidget formTypeId;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeId;

    @UiField
    RefBookMultiPickerModalWidget formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @UiField(provided = true)
    ValueListBox<AuditFormType> auditFormTypeId;

    @UiField
    RefBookMultiPickerModalWidget user;

    @UiField
    Panel declarationTypePanel;

    @UiField
    Panel formPanel;

    private static final int oneDayTime = 24 * 60 * 60 * 1000;

    private Map<Integer, String> declarationTypesMap;

    @Override
    public void setDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentSelectionTree.setAvalibleValues(list, availableValues);
    }

    @Override
    public void setDeclarationType(Map<Integer, String> declarationTypesMap) {
        this.declarationTypesMap = declarationTypesMap;
        declarationTypeId.setAcceptableValues(declarationTypesMap.keySet());
    }

    @Override
    public void setFormDataTaxType(List<TaxType> taxTypeList) {
        taxType.setAcceptableValues(taxTypeList);
    }

    @Override
    public void updateReportPeriodPicker(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public LogSystemFilter getFilterData() {
        LogSystemFilter lsf = new LogSystemFilter();
        // Отчетные периоды
        lsf.setReportPeriodIds(reportPeriodIds.getValue());
        // Подразделение
        if (departmentSelectionTree.getValue() != null && !departmentSelectionTree.getValue().isEmpty()) {
            lsf.setDepartmentId(departmentSelectionTree.getValue().get(0));
        }
        // Тип формы
        lsf.setAuditFormTypeId(auditFormTypeId.getValue() == null ? null : auditFormTypeId.getValue().getId());
        // Вид налоговой формы
        lsf.setFormTypeId(formTypeId.getValue().isEmpty()? null : Integer.valueOf(String.valueOf(formTypeId.getValue().get(0))));
        // Вид декларации
        lsf.setDeclarationTypeId(declarationTypeId.getValue());
        // Тип налоговой формы
        lsf.setFormKind(formDataKind.getValue().isEmpty()? null :FormDataKind.fromId(Integer.valueOf(String.valueOf(formDataKind.getValue().get(0)))));
        // Период
        lsf.setFromSearchDate(fromSearchDate.getValue());
        lsf.setToSearchDate(new Date(oneDayTime + toSearchDate.getValue().getTime()));
        // Пользователь
        lsf.setUserId(user.getSingleValue());
        lsf.setTaxType(taxType.getValue());

        return lsf;
    }

    /*@Override
    public void setFormTypeHandler(ValueChangeHandler<AuditFormType> handler) {
        auditFormTypeId.addValueChangeHandler(handler);
    }*/

    @Override
    public void init() {
        departmentSelectionTree.setValue(null, true);
        reportPeriodIds.setValue(null, true);
        taxType.setValue(null, true);
        auditFormTypeId.setValue(null, true);
        user.setValue(null, true);
        user.setDereferenceValue(null);
        formDataKind.setValue(null, true);
        formDataKind.setDereferenceValue(null);
        formTypeId.setValue(null, true);
        formTypeId.setDereferenceValue(null);
    }

    @Inject
    @UiConstructor
    public AuditFilterView(final Binder uiBinder) {

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

        declarationTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return declarationTypesMap.get(object);
            }
        });
        declarationTypeId.setEnabled(false);

        initWidget(uiBinder.createAndBindUi(this));
        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
        user.setEndDate(new Date());
        reportPeriodIds.setEnabled(false);
        formDataKind.setPeriodDates(new Date(), new Date());
        formTypeId.setEnabled(false);
    }

    @UiHandler("search")
    void onSearchButtonClicked(ClickEvent event) {
        if (getUiHandlers() != null)
            getUiHandlers().onSearchButtonClicked();
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
                        declarationTypeId.setValue(null);
                        declarationTypeId.setEnabled(true);
                        break;
                }
            }
        } else {
            formTypeId.setValue(null, true);
            formTypeId.setEnabled(false);
            declarationTypeId.setValue(null);
            declarationTypeId.setEnabled(false);
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
                declarationTypeId.setEnabled(true);
                setVisibleDeclarationFields();
                break;
        }
    }

    private void setVisibleTaxFields() {
        declarationTypeId.setValue(null);
        formPanel.setVisible(true);
        declarationTypePanel.setVisible(false);
    }

    private void setVisibleDeclarationFields() {
        /*formTypeId.setValue(new ArrayList<Long>());*/
        formDataKind.setValue(null, true);
        formPanel.setVisible(false);
        declarationTypePanel.setVisible(true);
    }

    private void hideAll() {
        formTypeId.setValue(null, true);
        formDataKind.setValue(null, true);
        declarationTypeId.setValue(null);
        formPanel.setVisible(false);
        declarationTypePanel.setVisible(false);
    }
}
