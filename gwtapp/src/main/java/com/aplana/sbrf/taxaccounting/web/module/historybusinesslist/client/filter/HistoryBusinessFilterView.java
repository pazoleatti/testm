package com.aplana.sbrf.taxaccounting.web.module.historybusinesslist.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.LogBusinessFilterValues;
import com.aplana.sbrf.taxaccounting.web.widget.datepicker.CustomDateBox;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPicker;
import com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client.RefBookPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: avanteev
 */
public class HistoryBusinessFilterView extends ViewWithUiHandlers<HistoryBusinessUIHandler> implements HistoryBusinessFilterPresenter.MyView,
        Editor<LogBusinessFilterValues> {


    interface Binder extends UiBinder<Widget, HistoryBusinessFilterView> { }

    @UiField
    PeriodPicker reportPeriodIds;

    @UiField
    CustomDateBox fromSearchDate;

    @UiField
    CustomDateBox toSearchDate;

    @Ignore
    @UiField
    DepartmentPickerPopupWidget departmentSelectionTree;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> declarationTypeIds;

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @Ignore
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

    private Map<Integer, String> formTypesMap;
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
        lbf.setFormTypeId(formTypeId.getValue());
        // Вид декларации
        lbf.setDeclarationTypeId(declarationTypeIds.getValue());
        // Тип налоговой формы
        lbf.setFormKind(formDataKind.getValue());
        // Период
        lbf.setFromSearchDate(fromSearchDate.getValue());
        lbf.setToSearchDate(new Date(oneDayTime + toSearchDate.getValue().getTime()));
        // Пользователь
        lbf.setUserId(user.getValue());
        lbf.setTaxType(taxType.getValue());
        return lbf;
    }

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
        declarationTypeIds.setAcceptableValues(declarationTypesMap.keySet());
    }

    @Override
    public void setFormDataKind(List<FormDataKind> list) {
        formDataKind.setAcceptableValues(list);
    }

    @Override
    public void setFormDataTaxType(List<TaxType> taxTypeList) {
        taxType.setAcceptableValues(taxTypeList);
    }

    @Override
    public void setReportPeriodPicker(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
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

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
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

        declarationTypeIds = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return declarationTypesMap.get(object);
            }
        });

        initWidget(binder.createAndBindUi(this));

        fromSearchDate.setValue(new Date());
        toSearchDate.setValue(new Date());
    }

    private void setVisibleTaxFields() {
        formTypePanel.setVisible(true);
        formKindPanel.setVisible(true);
        declarationTypePanel.setVisible(false);
        declarationTypeIds.setValue(null);
        formDataKind.setValue(FormDataKind.PRIMARY);
    }

    private void setVisibleDeclarationFields() {
        formTypePanel.setVisible(false);
        formKindPanel.setVisible(false);
        formTypeId.setValue(null);
        formDataKind.setValue(null);
        declarationTypePanel.setVisible(true);
    }

    private void hideAll() {
        formTypePanel.setVisible(false);
        formKindPanel.setVisible(false);
        formTypeId.setValue(null);
        formDataKind.setValue(null);
        declarationTypePanel.setVisible(false);
        declarationTypeIds.setValue(null);
    }

    @UiHandler("auditFormTypeId")
    public void onClick(ValueChangeEvent<AuditFormType> event){
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
    void onTaxTypeValueChange(ValueChangeEvent<TaxType> event){
        if (getUiHandlers() != null){
            getUiHandlers().getReportPeriods(event.getValue());
        }
    }
}
