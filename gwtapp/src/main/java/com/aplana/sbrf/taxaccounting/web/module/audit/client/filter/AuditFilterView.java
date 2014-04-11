package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.gwt.client.ListBoxWithTooltip;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.AuditFormType;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.audit.shared.LogSystemAuditFilter;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.DateMaskBoxPicker;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPicker;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
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
        implements AuditFilterPresenter.MyView, Editor<LogSystemAuditFilter> {

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    interface Binder extends UiBinder<Widget, AuditFilterView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<LogSystemAuditFilter, AuditFilterView>{}

    private MyDriver driver;

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

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeId;

    @Path("formKind")
    @UiField
    RefBookPicker formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @UiField(provided = true)
    ValueListBox<AuditFormType> auditFormTypeId;

    @Path("userIds")
    @UiField
    RefBookPicker user;

    @Editor.Ignore
    @UiField
    Button search;

    @Editor.Ignore
    @UiField
    Label formKindDecTypeLabel;

    @Editor.Ignore
    @UiField
    Label formTypeLabel;

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
    public LogSystemAuditFilter getFilterData() {
        return driver.flush();
    }

    @Override
    public boolean isChangeFilter() {
        return driver.isDirty();
    }

    @Override
    public void edit(LogSystemAuditFilter auditFilter) {
        driver.edit(auditFilter);
    }

    @Override
    public void clear() {
        taxType.setValue(null, true);
        formTypeId.setValue(null, true);
        formDataKind.setValue(null, true);
        declarationTypeId.setValue(null);

        formTypeId.setVisible(false);
        formDataKind.setVisible(false);
        formTypeLabel.setVisible(false);

        formKindDecTypeLabel.setVisible(false);

        declarationTypeId.setVisible(false);
    }

    @Override
    public void init() {
        driver.edit(new LogSystemAuditFilter());
    }

    @Inject
    @UiConstructor
    public AuditFilterView(final Binder uiBinder, MyDriver driver) {

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
        /*declarationTypeId.setEnabled(false);*/

        initWidget(uiBinder.createAndBindUi(this));
        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
        user.setPeriodDates(null, new Date());
        formDataKind.setPeriodDates(new Date(), new Date());
        /*formTypeId.setEnabled(false);*/
        formTypeId.setPeriodDates(new Date(), new Date());

        this.driver = driver;
        this.driver.initialize(this);
    }

    @UiHandler("search")
    void onSearchButtonClicked(ClickEvent event) {
        if (fromSearchDate.getValue() == null || toSearchDate.getValue() == null) {
            Dialog.errorMessage("Укажите корректную дату.");
            return;
        }

        if (fromSearchDate.getValue().compareTo(toSearchDate.getValue()) > 0) {
            Dialog.errorMessage("Операция \"Получение списка журнала аудита\" не выполнена. Дата \"От\" должна быть меньше или равна дате \"До\"");
            return;
        }

        if (getUiHandlers() != null)
            getUiHandlers().onSearchButtonClicked();
    }

    @UiHandler("taxType")
    void onTaxTypeValueChange(ValueChangeEvent<TaxType> event) {
        if (taxType.getValue() == null){
            reportPeriodIds.setValue(null, true);
            reportPeriodIds.setEnabled(false);
            formTypeId.setFilter(null);
            return;
        } else {
            formTypeId.setFilter("TAX_TYPE='" + taxType.getValue().getCode() + "'");
        }
        if (getUiHandlers() != null) {
            reportPeriodIds.setValue(null, true);
            getUiHandlers().getReportPeriods(event.getValue());
            reportPeriodIds.setEnabled(true);
        }
    }

    /*@UiHandler("reportPeriodIds")
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
    }*/

    @UiHandler("auditFormTypeId")
    public void onAuditFormTypeChange(ValueChangeEvent<AuditFormType> event){
        if (event.getValue() == null){
            hideAll();
            return;
        }
        switch (event.getValue()){
            case FORM_TYPE_TAX:
                /*if (reportPeriodIds.getValue() == null || reportPeriodIds.getValue().isEmpty()){
                    setVisibleTaxFields();
                    return;
                }
                Pair<Date, Date> datePair = reportPeriodIds.getPeriodDates(reportPeriodIds.getValue().get(0));
                formTypeId.setPeriodDates(datePair.getFirst(), datePair.getSecond());
                formTypeId.setEnabled(true);*/
                setVisibleTaxFields();
                break;
            case FORM_TYPE_DECLARATION:
                /*if (reportPeriodIds.getValue() == null || reportPeriodIds.getValue().isEmpty()){
                    setVisibleDeclarationFields();
                    return;
                }
                declarationTypeId.setEnabled(true);*/
                setVisibleDeclarationFields();
                break;
        }
    }

    private void setVisibleTaxFields() {
        declarationTypeId.setValue(null);

        formTypeId.setVisible(true);
        formDataKind.setVisible(true);
        formTypeLabel.setVisible(true);

        setLabelFormsType(true);

        declarationTypeId.setVisible(false);
    }

    private void setVisibleDeclarationFields() {
        /*formTypeId.setValue(new ArrayList<Long>());*/
        formDataKind.setValue(null, true);

        formTypeId.setVisible(false);
        formDataKind.setVisible(false);
        formTypeLabel.setVisible(false);

        setLabelFormsType(false);

        declarationTypeId.setVisible(true);
    }

    private void hideAll() {
        formTypeId.setValue(null, true);
        formDataKind.setValue(null, true);
        declarationTypeId.setValue(null);

        formTypeId.setVisible(false);
        formDataKind.setVisible(false);
        formTypeLabel.setVisible(false);

        declarationTypeId.setVisible(false);
        formKindDecTypeLabel.setVisible(false);
    }

    /**
     * Переключить текст лейбла на вид делариции или тип НФ
     * @param isTax true если НФ
     */
    private void setLabelFormsType(boolean isTax){
        formKindDecTypeLabel.setVisible(true);
        formKindDecTypeLabel.setText(isTax ? "Тип нал. формы:" : "Вид декларации:");
    }
}
