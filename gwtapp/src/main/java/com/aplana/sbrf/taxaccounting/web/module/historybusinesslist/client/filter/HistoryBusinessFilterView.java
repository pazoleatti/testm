package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.util.Pair;
import com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.shared.LogSystemAuditFilter;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPicker;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
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
        Editor<LogSystemAuditFilter> {


    interface Binder extends UiBinder<Widget, HistoryBusinessFilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<LogSystemAuditFilter, HistoryBusinessFilterView> {}

    private MyDriver driver = GWT.create(MyDriver.class);

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    DateMaskBoxPicker fromSearchDate;

    @UiField
    DateMaskBoxPicker toSearchDate;

    @Path("departmentIds")
    @UiField
    DepartmentPickerPopupWidget departmentSelectionTree;

    @Path("formTypeIds")
    @UiField
    RefBookPicker formTypeId;

    @Path("declarationTypeId")
    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeIds;

    @Path("formKind")
    @UiField
    RefBookPicker formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @UiField(provided = true)
    ValueListBox<AuditFormType> auditFormTypeId;

    @UiField
    Panel formPanel;

    @UiField
    Panel declarationTypePanel;

    @Path("userIds")
    @UiField
    RefBookPicker user;

    private Map<Integer, String> declarationTypesMap;
    private static final int oneDayTime = 24 * 60 * 60 * 1000;

    @Override
    public void init() {
        driver.edit(new LogSystemAuditFilter());
    }

    @Override
    public LogSystemAuditFilter getDataFilter() {
        return driver.flush();
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
    public boolean isChangeFilter() {
        return driver.isDirty();
    }

    @Override
    public void edit(LogSystemAuditFilter auditFilter) {
        driver.edit(auditFilter);
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
        user.setPeriodDates(null, new Date());
        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        reportPeriodIds.setEnabled(false);
        formTypeId.setEnabled(false);

        driver.initialize(this);
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
    void onSearchButtonClicked(ClickEvent event) {
        Date fromDate = fromSearchDate.getDateBox().getRawValue();
        Date toDate = toSearchDate.getDateBox().getRawValue();

        if (fromDate == null || toDate == null) {
            Dialog.errorMessage("Ошибка", "Укажите корректную дату");
            return;
        }

        if (fromDate !=null && toDate != null) {
            if (fromSearchDate.getValue().compareTo(toSearchDate.getValue()) > 0) {
                Dialog.errorMessage("Ошибка", "Операция \"Получение списка журнала аудита\" не выполнена. Дата \"От\" должна быть меньше или равна дате \"До\"");
                return;
            }
        }

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
        } else {
            formTypeId.setFilter("TAX_TYPE='" + taxType.getValue().getCode() + "'");
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
