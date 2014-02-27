package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.ValueListBox;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookPickerWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.*;

public class CreateFormDataView extends PopupViewWithUiHandlers<CreateFormDataUiHandlers> implements CreateFormDataPresenter.MyView,
        Editor<FormDataFilter> {

    public interface Binder extends UiBinder<PopupPanel, CreateFormDataView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<FormDataFilter, CreateFormDataView> {
    }

    private final MyDriver driver;

    @UiField
    @Path("departmentIds")
    DepartmentPickerPopupWidget departmentPicker;

    @UiField
    PeriodPickerPopupWidget reportPeriodIds;

    @UiField
    RefBookPickerWidget formDataKind;

    @UiField
    RefBookPickerWidget formTypeId;

    @UiField(provided = true)
    ValueListBox<Months> formMonth;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        formMonth = new ValueListBox<Months>(new AbstractRenderer<Months>() {
            @Override
            public String render(Months object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);

        // Нельзя аннотацией, баг GWT https://code.google.com/p/google-web-toolkit/issues/detail?id=6091
//        formTypeId.addValueChangeHandler(new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Integer> event) {
//                updateEnabled();
//            }
//        });
//
//        formDataKind.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<List<Long>> event) {
//                if (formDataKind.getValue() == null || formDataKind.getValue().isEmpty()){
//                    formTypeId.setValue(null);
//                    updateEnabled();
//                    return;
//                }
//                if (getUiHandlers() != null){
//                    getUiHandlers().onFormKindChange();
//                    updateEnabled();
//                }
//            }
//        });

        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
        formTypeId.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void init() {
        // Сброс состояния формы
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        formDataKind.setValue(new ArrayList<Long>(), true);
        formDataKind.setDereferenceValue(null);
        formTypeId.setValue(new ArrayList<Long>(), true);
        formTypeId.setDereferenceValue(null);
        formMonth.setValue(null);
        updateEnabled();
    }

    private void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue().size() != 0);
        // "Месяц" недоступен если не выбран "Вид налоговой формы"
        formMonth.setEnabled(formTypeId.getValue() != null);
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(formMonth.getValue() != null);
    }


    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        getUiHandlers().onReportPeriodChange();
    }

    @UiHandler("departmentPicker")
    public void onDepartmentChange(ValueChangeEvent<List<Integer>> event) {
        formDataKind.setValue(new ArrayList<Long>(), true);
        formDataKind.setDereferenceValue(null);
        formTypeId.setValue(new ArrayList<Long>(), true);
        formTypeId.setDereferenceValue(null);
        updateEnabled();
    }

    @UiHandler("formDataKind")
    public void onDataKindChange(ValueChangeEvent<List<Long>> event) {
        formTypeId.setValue(new ArrayList<Long>(), true);
        formTypeId.setDereferenceValue(null);
        updateEnabled();
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<List<Long>> event) {
        formMonth.setValue(null, true);
        getUiHandlers().isMonthly(formTypeId.getValue().get(0).intValue(), reportPeriodIds.getValue().get(0));
        updateEnabled();
    }

    @UiHandler("continueButton")
    public void onSave(ClickEvent event) {
        getUiHandlers().onConfirm();
    }

    @UiHandler("cancelButton")
    public void onCancel(ClickEvent event) {
        Dialog.confirmMessage("Создание налоговой формы", "Хотите отменить создание налоговой формы?", new DialogHandler() {
            @Override
            public void yes() {
                Dialog.hideMessage();
                hide();
            }
        });
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @UiHandler("formMonth")
    public void onChangeFormMonth(ValueChangeEvent<Months> event) {
        updateEnabled();
    }

    @Override
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentPicker.setAvalibleValues(list, availableValues);
    }

    @Override
    public void setFilterData(FormDataFilter filter) {
        driver.edit(filter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //departmentPicker.setValue(filter.getDepartmentIds());
    }

    @Override
    public FormDataFilter getFilterData() {
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //filter.setDepartmentIds(departmentPicker.getValue());
        return driver.flush();
    }

    @Override
    public void setFilter(String filter) {
        formTypeId.setFilter(filter);
    }

    @Override
    public void setAcceptableMonthList(List<Months> monthList) {
        formMonth.setValue(null, true);
        formMonth.setAcceptableValues(monthList);

    }

    @Override
    public void setFormMonthEnabled(boolean isMonthly) {
        // Если ежемесячный, то устанавливается formMonth = true
        formMonth.setEnabled(isMonthly);
        // Кнопка "Создать" пока неактивна
        if (isMonthly) {
            continueButton.setEnabled(false);
            // Иначе, кнопка активна
        } else {
            continueButton.setEnabled(true);
        }
    }
}
