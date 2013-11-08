package com.aplana.sbrf.taxaccounting.web.module.audit.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    PeriodPicker reportPeriodIds;

    interface Binder extends UiBinder<Widget, AuditFilterView> { }

    @UiField
    CustomDateBox fromSearchDate;

    @UiField
    CustomDateBox toSearchDate;

    @UiField
    DepartmentPickerPopupWidget departmentSelectionTree;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeId;

    @UiField(provided = true)
    ValueListBox<FormDataKind> formKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @UiField(provided = true)
    ValueListBox<AuditFormType> auditFormTypeId;

    @UiField
    Panel formKindPanel;

    @UiField
    Panel formTypePanel;

    @UiField
    Panel declarationTypePanel;

    @UiField
    RefBookPickerPopupWidget user;

	private static final int oneDayTime = 24 * 60 * 60 * 1000;

    private Map<Integer, String> formTypesMap;
    private Map<Integer, String> declarationTypesMap;

    @Override
    public void setDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentSelectionTree.setAvalibleValues(list, availableValues);
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
        lsf.setFormTypeId(formTypeId.getValue());
        // Вид декларации
        lsf.setDeclarationTypeId(declarationTypeId.getValue());
        // Тип налоговой формы
        lsf.setFormKind(formKind.getValue());
        // Период
        lsf.setFromSearchDate(fromSearchDate.getValue());
        lsf.setToSearchDate(new Date(oneDayTime + toSearchDate.getValue().getTime()));
        // Пользователь
        lsf.setUserId(user.getValue());
        lsf.setTaxType(taxType.getValue());
        return lsf;
    }

    @Override
    public void setVisibleTaxFields() {
        formTypePanel.setVisible(true);
        formKindPanel.setVisible(true);
        declarationTypePanel.setVisible(false);
        declarationTypeId.setValue(null);
        formKind.setValue(FormDataKind.PRIMARY);
    }

    @Override
    public void setVisibleDeclarationFields() {
        formTypePanel.setVisible(false);
        formKindPanel.setVisible(false);
        formTypeId.setValue(null);
        formKind.setValue(null);
        declarationTypePanel.setVisible(true);
    }

    @Override
    public void hideAll() {
        formTypePanel.setVisible(false);
        formKindPanel.setVisible(false);
        formTypeId.setValue(null);
        formKind.setValue(null);
        declarationTypePanel.setVisible(false);
        declarationTypeId.setValue(null);
    }

    @Override
    public void setValueListBoxHandler(ValueChangeHandler<TaxType> handler) {
        taxType.addValueChangeHandler(handler);
    }

    @Override
    public void setFormTypeHandler(ValueChangeHandler<AuditFormType> handler) {
        auditFormTypeId.addValueChangeHandler(handler);
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

        formKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        taxType = new ValueListBox<TaxType>(new AbstractRenderer<TaxType>() {
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

        initWidget(uiBinder.createAndBindUi(this));
        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
    }

    @UiHandler("search")
    void onSearchButtonClicked(ClickEvent event){
        if(getUiHandlers() != null)
            getUiHandlers().onSearchButtonClicked();
    }

    @UiHandler("printButton")
    void onPrintButtonClicked(ClickEvent event){
        getUiHandlers().onPrintButtonClicked();
    }

    @UiHandler("archive")
    void onArchive(ClickEvent event){
        if(getUiHandlers() != null){
            getUiHandlers().onArchiveButtonClicked();
        }
    }
}
