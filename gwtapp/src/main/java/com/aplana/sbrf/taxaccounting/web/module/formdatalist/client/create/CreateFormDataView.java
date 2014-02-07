package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.gwt.client.ListBoxWithTooltipWidget;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.gwt.client.ValueListBox;
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

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ListBoxWithTooltipWidget<Integer> formTypeId;

    @UiField(provided = true)
    ValueListBox<Months> formMonth;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formTypeId = new ListBoxWithTooltipWidget<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

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
        // Пока закоментил, нужно разобраться
//        formTypeId.addValueChangeHandler(new ValueChangeHandler<Integer>() {
//            @Override
//            public void onValueChange(ValueChangeEvent<Integer> event) {
//                updateEnabled();
//            }
//        });
    }

    @Override
    public void init() {
        // Сброс состояния формы
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        formDataKind.setValue(null);
        formTypeId.setValue(null);
        formMonth.setValue(null);
        updateEnabled(false);
    }

    private void updateEnabled(boolean isMonthly) {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue() != null);
        // "Месяц" недоступен если не выбран "Вид налоговой формы"
        formMonth.setEnabled(formTypeId.getValue() != null && isMonthly);
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(formMonth.getValue() != null || (formTypeId.getValue() != null && !isMonthly));
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

    @UiHandler("reportPeriodIds")
    public void onReportPeriodChange(ValueChangeEvent<List<Integer>> event) {
        departmentPicker.setValue(null, true);
        updateEnabled(false);
    }

    @UiHandler("departmentPicker")
    public void onDepartmentChange(ValueChangeEvent<List<Integer>> event) {
        formDataKind.setValue(null, true);
        updateEnabled(false);
    }

    @UiHandler("formDataKind")
    public void onFormKindChange(ValueChangeEvent<FormDataKind> event) {
        formTypeId.setValue(null, true);
        updateEnabled(false);
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<Integer> event) {
        formMonth.setValue(null, true);
        updateEnabled(getUiHandlers().isMonthly());
    }

    @UiHandler("formMonth")
    public void onChangeFormMonth(ValueChangeEvent<Months> event) {
        updateEnabled(getUiHandlers().isMonthly());
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentPicker.setAvalibleValues(list, availableValues);
    }

    @Override
    public void setAcceptableFormKindList(List<FormDataKind> list) {
        formDataKind.setValue(null, true);
        formDataKind.setAcceptableValues(list);
    }

    @Override
    public void setAcceptableFormTypeList(List<FormType> formTypes) {
        formTypesMap.clear();
        for (FormType formType : formTypes) {
            formTypesMap.put(formType.getId(), formType.getName());
        }

        formTypeId.setValue(null);
        formTypeId.setAcceptableValues(formTypesMap.keySet());
    }

    @Override
    public void setAcceptableMonthList(List<Months> monthList) {
        formMonth.setValue(null, true);
        formMonth.setAcceptableValues(monthList);
    }

    @Override
    public FormDataFilter getFilterData() {
        FormDataFilter filter = driver.flush();
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //filter.setDepartmentIds(departmentPicker.getValue());
        return filter;
    }

    @Override
    public void setFilterData(FormDataFilter filter) {
        driver.edit(filter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //departmentPicker.setValue(filter.getDepartmentIds());
    }
}
