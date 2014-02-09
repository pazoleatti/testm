package com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.create;

import com.aplana.gwt.client.ListBoxWithTooltipWidget;
import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.gwt.client.dialog.DialogHandler;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.periodpicker.client.PeriodPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.RefBookMultiPickerModalWidget;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
    RefBookMultiPickerModalWidget formDataKind;

    @UiField(provided = true)
    ListBoxWithTooltipWidget<Integer> formTypeId;

    @UiField
    Button continueButton;

    @UiField
    Button cancelButton;

    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

    @Inject
    public CreateFormDataView(Binder uiBinder, final MyDriver driver, EventBus eventBus) {
        super(eventBus);

        formTypeId = new ListBoxWithTooltipWidget<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

        initWidget(uiBinder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);

        // Нельзя аннотацией, баг GWT https://code.google.com/p/google-web-toolkit/issues/detail?id=6091
        formTypeId.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateEnabled();
            }
        });

        formDataKind.addValueChangeHandler(new ValueChangeHandler<List<Long>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Long>> event) {
                if (formDataKind.getValue() == null || formDataKind.getValue().isEmpty()){
                    formTypeId.setValue(null);
                    updateEnabled();
                    return;
                }
                if (getUiHandlers() != null){
                    getUiHandlers().onFormKindChange();
                    updateEnabled();
                }
            }
        });

        // т.к. справочник не версионный, а дату выставлять обязательно
        formDataKind.setPeriodDates(new Date(), new Date());
    }

    @Override
    public void init() {
        // Сброс состояния формы
        reportPeriodIds.setValue(null);
        departmentPicker.setValue(null);
        formDataKind.setValue(new ArrayList<Long>());
        formTypeId.setValue(null);
        updateEnabled();
    }

    private void updateEnabled() {
        // "Подразделение" недоступно если не выбран отчетный период
        departmentPicker.setEnabled(reportPeriodIds.getValue() != null && !reportPeriodIds.getValue().isEmpty());
        // "Тип налоговой формы" недоступен если не выбрано подразделение
        formDataKind.setEnabled(departmentPicker.getValue() != null && !departmentPicker.getValue().isEmpty());
        // "Вид налоговой формы" недоступен если не выбран тип НФ
        formTypeId.setEnabled(formDataKind.getValue().size() != 0);
        // Кнопка "Создать" недоступна пока все не заполнено
        continueButton.setEnabled(formTypeId.getValue() != null);
    }

    @Override
    public void setAcceptableDepartments(List<Department> list, Set<Integer> availableValues) {
        departmentPicker.setAvalibleValues(list, availableValues);
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
        /*departmentPicker.setValue(null, true);
        updateEnabled();*/
        if (getUiHandlers() != null){
            getUiHandlers().onReportPeriodChange();
            updateEnabled();
        }
    }

    @UiHandler("departmentPicker")
    public void onDepartmentChange(ValueChangeEvent<List<Integer>> event) {
        //formDataKind.setValue(null, true);
        if (departmentPicker.getValue() == null || departmentPicker.getValue().isEmpty()){
            updateEnabled();
            return;
        }
        if (getUiHandlers() != null){
            getUiHandlers().onDepartmentChange();
            updateEnabled();
        }
    }

    @UiHandler("formTypeId")
    public void onFormTypeIdChange(ValueChangeEvent<Integer> event) {
//        continueButton.setEnabled(t);
        updateEnabled();
    }

    @Override
    public void setAcceptableFormKindList(List<FormDataKind> list) {
        formDataKind.setValue(null, true);
        List<Long> longs = new ArrayList<Long>();
        for (FormDataKind kind : list){
            longs.add((long) kind.getId());
        }
        formDataKind.setValue(longs);
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
    public FormDataFilter getFilterData() {
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //filter.setDepartmentIds(departmentPicker.getValue());
        return driver.flush();
    }

    @Override
    public void setAcceptableReportPeriods(List<ReportPeriod> reportPeriods) {
        reportPeriodIds.setPeriods(reportPeriods);
    }

    @Override
    public void setFilterData(FormDataFilter filter) {
        driver.edit(filter);
        // DepartmentPiker не реализует asEditor, поэтому сетим значение руками.
        //departmentPicker.setValue(filter.getDepartmentIds());
    }
}
